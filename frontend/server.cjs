const http = require("node:http");
const https = require("node:https");
const fs = require("node:fs");
const path = require("node:path");

const frontendRoot = __dirname;
const port = Number(process.env.PORT || 8000);
const apiBase = new URL(process.env.JOURNEYFLOW_API_URL || "http://localhost:8080");
const mimeTypes = {
  ".css": "text/css; charset=utf-8",
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".png": "image/png",
  ".svg": "image/svg+xml"
};

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

  fs.stat(filePath, (error, stats) => {
    if (error || !stats.isFile()) {
      sendJson(response, 404, { message: "Arquivo não encontrado." });
      return;
    }
    response.writeHead(200, {
      "Content-Type": mimeTypes[path.extname(filePath).toLowerCase()] || "application/octet-stream"
    });
    fs.createReadStream(filePath).pipe(response);
  });
});

server.listen(port, "127.0.0.1", () => {
  console.log(`JourneyFlow frontend: http://localhost:${port}`);
  console.log(`API proxy: ${apiBase.origin}`);
});
