import assert from "node:assert/strict";
import test from "node:test";
import {
  bearingDegrees,
  buildNearbyOverpassQuery,
  haversineMeters,
  nearbyPlaceCacheKey,
  normalizeNearbyPlaces,
  roundForNearbyQuery,
} from "./nearbyPlaces";

test("cache keys round location so exact user coordinates are not used as cache identifiers", () => {
  assert.equal(nearbyPlaceCacheKey(43.653271, -79.383081, 250), nearbyPlaceCacheKey(43.65329, -79.38309, 250));
  assert.notEqual(nearbyPlaceCacheKey(43.653, -79.383, 250), nearbyPlaceCacheKey(43.663, -79.383, 250));
  assert.equal(roundForNearbyQuery(43.653271), 43.653);
});

test("Overpass query bounds distance and filters to named outdoor/cultural/public-interest places", () => {
  const query = buildNearbyOverpassQuery(43.653271, -79.383081, 250);
  assert.match(query, /around:250,43\.653,-79\.383/);
  assert.match(query, /\[historic\]/);
  assert.match(query, /museum\|viewpoint/);
  assert.doesNotMatch(query, /description\]/);
  assert.match(buildNearbyOverpassQuery(43.65, -79.38, 5000), /around:500,/);
});

test("distance and bearing calculations are plausible", () => {
  assert.ok(Math.abs(haversineMeters({lat: 0, lon: 0}, {lat: 0, lon: 0.001}) - 111) < 2);
  assert.ok(Math.abs(bearingDegrees({lat: 43, lon: -79}, {lat: 43.001, lon: -79}) - 0) < 0.1);
  assert.ok(Math.abs(bearingDegrees({lat: 43, lon: -79}, {lat: 43, lon: -79.001}) - 270) < 0.1);
});

test("normalization filters unnamed and distant elements and returns source-linked, activity-neutral results", () => {
  const results = normalizeNearbyPlaces({
    elements: [
      { type: "node", id: 100, lat: 43.653, lon: -79.383, tags: { name: "Old Town Hall", historic: "building" } },
      { type: "way", id: 101, center: { lat: 43.6535, lon: -79.383, }, tags: { name: "Community Garden", leisure: "garden" } },
      { type: "node", id: 102, lat: 43.7, lon: -79.3, tags: { name: "Far Away", tourism: "attraction" } },
      { type: "node", id: 103, lat: 43.6531, lon: -79.3831, tags: { tourism: "viewpoint" } },
    ],
  }, { lat: 43.653, lon: -79.383, headingDeg: 0, speedMps: 1, radiusMeters: 250 });

  assert.equal(results.length, 2);
  assert.ok(results.some((place) => place.name === "Old Town Hall"));
  assert.ok(results.every((place) => place.mapUrl.startsWith("https://www.openstreetmap.org/")));
  assert.ok(results.some((place) => place.relativeClock !== undefined));
  assert.ok(results.every((place) => !("description" in place)));
});

test("relative direction is omitted when heading is stale or movement is too slow", () => {
  const payload = {
    elements: [{ type: "node", id: 1, lat: 43.654, lon: -79.383, tags: { name: "North Place", tourism: "attraction" } }],
  };
  const results = normalizeNearbyPlaces(payload, { lat: 43.653, lon: -79.383, headingDeg: 0, speedMps: 0.1, radiusMeters: 250 });
  assert.equal(results.length, 1);
  assert.equal(results[0].relativeClock, undefined);
});
