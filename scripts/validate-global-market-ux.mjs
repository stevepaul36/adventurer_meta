import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import vm from "node:vm";

const path = resolve("prototypes/global-market-aware-ux/index.html");
const html = readFileSync(path, "utf8");
const errors = [];
const pass = (message) => console.log("PASS:", message);
const fail = (message) => { errors.push(message); console.error("FAIL:", message); };

const scriptMatches = [...html.matchAll(/<script>([\s\S]*?)<\/script>/gi)];
if (scriptMatches.length !== 1) fail("Exactly one inline script exists.");
else {
  try { new vm.Script(scriptMatches[0][1], { filename: "global-market-aware-ux.js" }); pass("Inline JavaScript parses."); }
  catch (error) { fail("Inline JavaScript parses: " + error.message); }
}

const ids = [...html.matchAll(/\bid=["']([^"']+)["']/g)].map((m) => m[1]);
const duplicates = [...new Set(ids.filter((id, i) => ids.indexOf(id) !== i))];
if (duplicates.length) fail("HTML IDs are unique: " + duplicates.join(", "));
else pass("HTML IDs are unique.");

for (const [label, token] of [
  ["India market", 'IN:{name:"India"'],
  ["Canada market", 'CA:{name:"Canada"'],
  ["United States market", 'US:{name:"United States"'],
  ["Shared media viewer", 'id="feed"'],
  ["Market selector", 'id="market"'],
  ["Region selector", 'id="region"'],
  ["Activity context", 'id="activity"'],
  ["Unknown policy feed warning", "No live authority feed connected"],
  ["Explicit demo-only warning", "No official rules API is connected"],
]) {
  if (html.includes(token)) pass(label + " exists.");
  else fail(label + " exists.");
}
for (const [label, token] of [
  ["full-screen viewer open function", "function openViewer()"],
  ["Escape-to-close interaction", 'document.addEventListener("keydown"'],
  ["overlay backdrop close interaction", 'if(event.target===$("viewerOverlay"))closeViewer()'],
]) {
  if (html.includes(token)) pass(label + " exists.");
  else fail(label + " exists.");
}
for (const forbidden of ["AIza", "sk_live_", "sk_test_", "REVENUECAT_SECRET_API_KEY"]) {
  if (html.includes(forbidden)) fail("No credential-shaped or server-secret string in the client HTML: " + forbidden);
}
if (errors.length) process.exit(1);
console.log("Global market UX validation complete.");
