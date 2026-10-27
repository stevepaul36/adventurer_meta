import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import vm from "node:vm";

const htmlPath = resolve("prototypes/india-canada-market-preview/index.html");
const html = readFileSync(htmlPath, "utf8");
const fail = (message) => {
  console.error("FAIL:", message);
  process.exitCode = 1;
};
const pass = (message) => console.log("PASS:", message);

const scriptMatch = html.match(/<script>([\s\S]*?)<\/script>/i);
if (!scriptMatch) {
  fail("An inline JavaScript block exists.");
} else {
  try {
    new vm.Script(scriptMatch[1], { filename: "market-preview-inline.js" });
    pass("Inline JavaScript parses.");
  } catch (error) {
    fail("Inline JavaScript parses: " + error.message);
  }
}

const ids = [...html.matchAll(/\bid=["']([^"']+)["']/g)].map((match) => match[1]);
const duplicated = [...new Set(ids.filter((id, index) => ids.indexOf(id) !== index))];
if (duplicated.length) fail("Static HTML IDs are unique: duplicate " + duplicated.join(", "));
else pass("Static HTML IDs are unique.");

const requiredIds = [
  "page-journey", "page-explore", "page-plan", "page-memories", "page-voice",
  "marketIN", "marketCA", "fuelAdvice", "providerGrid", "modalWrap", "chatForm",
];
const missingIds = requiredIds.filter((id) => !ids.includes(id));
if (missingIds.length) fail("Required interactive regions exist: missing " + missingIds.join(", "));
else pass("All required navigation and interaction regions exist.");

const requiredBehaviours = [
  ["market selector", "function setMarket("],
  ["provider catalogue", "const PROVIDERS ="],
  ["India food providers", 'name:"Swiggy"'],
  ["Canada food providers", 'name:"Uber Eats"'],
  ["Canada park information", 'name:"Parks Canada"'],
  ["Canada EV charging handoff", 'name:"PlugShare"'],
  ["fuel calculation", "function calcFuel("],
  ["invalid fuel validation", "Correct the fuel inputs before saving an estimate."],
  ["local stop editing", "function addStop("],
  ["trip-pack export", "function exportPlan("],
  ["private memory flow", "function saveMemory("],
  ["memory deletion", "function deleteMemory("],
  ["deterministic voice responses", "function sendChat("],
  ["simulated transaction confirmation", "DEMO ORDER PLACED — SIMULATION ONLY"],
  ["provider handoff status", "EXTERNAL HANDOFF ONLY"],
  ["visible demo data warning", "SAMPLE DATA · NO LIVE BOOKINGS OR ORDERS"],
  ["INR/CAD-safe labels", 'currency:"INR"'],
  ["INR/CAD-safe labels", 'currency:"CAD"'],
  ["local persistence", "localStorage.setItem("],
];
for (const [label, needle] of requiredBehaviours) {
  if (html.includes(needle)) pass("Feature present: " + label + ".");
  else fail("Feature present: " + label + ".");
}

const navPages = [...html.matchAll(/data-page="([^"]+)"/g)].map((match) => match[1]);
const absentPages = [...new Set(navPages)].filter((page) => !ids.includes("page-" + page));
if (absentPages.length) fail("Every navigation tab has a matching page: missing " + absentPages.join(", "));
else pass("Every navigation tab maps to a page.");

const unsafeApis = ["navigator.geolocation", "getCurrentPosition(", "getUserMedia("];
const foundUnsafeApi = unsafeApis.filter((needle) => html.includes(needle));
if (foundUnsafeApi.length) fail("No automatic location/microphone API: found " + foundUnsafeApi.join(", "));
else pass("No automatic location or microphone API calls found.");

if (process.exitCode) process.exit(process.exitCode);
console.log("Market preview validation complete.");
