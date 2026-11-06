const http = require("node:http");
const https = require("node:https");
const fs = require("node:fs");
const path = require("node:path");

const frontendRoot = __dirname;
const port = Number(process.env.PORT) || 8000;
const apiBase = new URL(process.env.JOURNEYFLOW_API_URL || "http://localhost:8080");
const host = process.env.HOST || "127.0.0.1";
const mimeTypes = {
  ".css": "text/css; charset=utf-8",
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".png": "image/png",
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".webp": "image/webp",
  ".gif": "image/gif",
  ".ico": "image/x-icon",
  ".svg": "image/svg+xml",
  ".woff2": "font/woff2",
  ".map": "application/json; charset=utf-8"
};
// Só estes caminhos são públicos. Antes qualquer arquivo da pasta era servido,
// inclusive server.cjs e package.json.
const publicEntries = ["index.html", "css", "js", "data", "assets"];

function isPublicPath(relativePath) {
  const [first] = relativePath.split(/[\\/]/);
  return publicEntries.includes(first);
}

function sendJson(response, statusCode, body) {
  response.writeHead(statusCode, { "Content-Type": "application/json; charset=utf-8" });
  response.end(JSON.stringify(body));
}

function proxyApi(request, response, requestUrl) {
  const apiPath = requestUrl.pathname.slice("/__api".length) || "/";
  const upstreamUrl = new URL(`${apiPath}${requestUrl.search}`, apiBase);
  const headers = { ...request.headers, host: upstreamUrl.host };
  delete headers.connection;

  const transport = upstreamUrl.protocol === "https:" ? https : http;
  const upstream = transport.request(upstreamUrl, {
    method: request.method,
    headers
  }, (upstreamResponse) => {
    response.writeHead(upstreamResponse.statusCode || 502, upstreamResponse.headers);
    upstreamResponse.pipe(response);
  });

  upstream.on("error", () => {
    if (!response.headersSent) {
      sendJson(response, 502, {
        message: `Não foi possível conectar à API configurada em ${apiBase.origin}.`
      });
    } else {
      response.destroy();
    }
  });
  request.pipe(upstream);
}

const server = http.createServer((request, response) => {
  const requestUrl = new URL(request.url, `http://${request.headers.host || "localhost"}`);
  if (requestUrl.pathname === "/__api" || requestUrl.pathname.startsWith("/__api/")) {
    proxyApi(request, response, requestUrl);
    return;
  }

  if (request.method !== "GET" && request.method !== "HEAD") {
    response.writeHead(405, { Allow: "GET, HEAD", "Content-Type": "application/json; charset=utf-8" });
    response.end(JSON.stringify({ message: "Método não permitido." }));
    return;
  }

  let pathname;
  try {
    pathname = decodeURIComponent(requestUrl.pathname);
  } catch {
    sendJson(response, 400, { message: "Caminho inválido." });
    return;
  }
  const relativePath = pathname === "/" ? "index.html" : pathname.replace(/^[/\\]+/, "");
  const filePath = path.resolve(frontendRoot, relativePath);
  if (filePath !== frontendRoot && !filePath.startsWith(`${frontendRoot}${path.sep}`)) {
    sendJson(response, 403, { message: "Acesso negado." });
    return;
  }
  if (!isPublicPath(relativePath)) {
    sendJson(response, 404, { message: "Arquivo não encontrado." });
    return;
  }

  fs.stat(filePath, (error, stats) => {
    if (error || !stats.isFile()) {
      sendJson(response, 404, { message: "Arquivo não encontrado." });
      return;
    }

    // ETag simples: o navegador revalida a cada carga (sempre vê o código novo em desenvolvimento)
    // mas recebe 304 sem baixar o arquivo de novo, o que deixa a navegação mais rápida.
    const etag = `W/"${stats.size.toString(16)}-${Math.floor(stats.mtimeMs).toString(16)}"`;
    const headers = {
      "Content-Type": mimeTypes[path.extname(filePath).toLowerCase()] || "application/octet-stream",
      "Cache-Control": "no-cache",
      ETag: etag,
      "X-Content-Type-Options": "nosniff"
    };
    if (request.headers["if-none-match"] === etag) {
      response.writeHead(304, headers);
      response.end();
      return;
    }
    headers["Content-Length"] = stats.size;
    response.writeHead(200, headers);
    if (request.method === "HEAD") {
      response.end();
      return;
    }
    fs.createReadStream(filePath)
      .on("error", () => response.destroy())
      .pipe(response);
  });
});

server.listen(port, host, () => {
  console.log(`JourneyFlow frontend: http://${host === "127.0.0.1" ? "localhost" : host}:${port}`);
  console.log(`API proxy: ${apiBase.origin}`);
});
