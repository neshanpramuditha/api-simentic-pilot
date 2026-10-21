// Transparent recording proxy: forwards every request to the target API and writes the exchange to a HAR file after
// each response. Used as ground truth for what the semantic validator actually sent and received (including its own
// read-back GETs), independent of the validator's report.
// Usage: node record-proxy.js <listen port> <target base url> <out traffic.har.json>
const http = require('http');
const fs = require('fs');
const [port, target, outFile] = process.argv.slice(2);
const entries = [];
const save = () => fs.writeFileSync(outFile, JSON.stringify({ log: { version: '1.2', creator: { name: 'record-proxy', version: '1' }, entries } }, null, 2));
save();

http.createServer((req, res) => {
  const chunks = [];
  req.on('data', c => chunks.push(c));
  req.on('end', async () => {
    const started = new Date();
    const body = Buffer.concat(chunks).toString('utf8');
    const headers = {};
    for (const h of ['accept', 'content-type']) if (req.headers[h]) headers[h] = req.headers[h];
    try {
      const upstream = await fetch(target + req.url, { method: req.method, headers, body: body.length ? body : undefined });
      const text = await upstream.text();
      const type = upstream.headers.get('content-type') || '';
      entries.push({
        startedDateTime: started.toISOString(),
        time: Date.now() - started.getTime(),
        request: {
          method: req.method, url: target + req.url, httpVersion: 'HTTP/1.1',
          headers: Object.entries(headers).map(([name, value]) => ({ name, value })),
          ...(body.length ? { postData: { mimeType: headers['content-type'] || '', text: body } } : {}),
        },
        response: { status: upstream.status, statusText: upstream.statusText, httpVersion: 'HTTP/1.1',
          headers: type ? [{ name: 'content-type', value: type }] : [], content: { size: text.length, mimeType: type, text } },
      });
      save();
      res.writeHead(upstream.status, type ? { 'content-type': type } : {});
      res.end(text);
    } catch (e) {
      res.writeHead(502);
      res.end(String(e));
    }
  });
}).listen(Number(port), () => console.log(`record-proxy :${port} -> ${target}`));
