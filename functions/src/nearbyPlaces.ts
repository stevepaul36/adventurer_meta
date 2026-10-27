export interface NearbyAdventurePlace {
  id: string;
  name: string;
  category: string;
  lat: number;
  lon: number;
  distanceMeters: number;
  bearingDeg: number;
  relativeClock?: string;
  relativeDirection?: string;
  mapUrl: string;
}

export interface NearbyPlaceOptions {
  lat: number;
  lon: number;
  headingDeg?: number;
  speedMps?: number;
  radiusMeters?: number;
}

const CATEGORY_KEYS = ["historic", "tourism", "amenity", "natural", "leisure", "shop", "man_made", "building"] as const;
const CARDINAL_CLOCK = ["12 o'clock", "1 o'clock", "2 o'clock", "3 o'clock", "4 o'clock", "5 o'clock", "6 o'clock", "7 o'clock", "8 o'clock", "9 o'clock", "10 o'clock", "11 o'clock"];

export function roundForNearbyQuery(value: number): number {
  return Math.round(value * 1000) / 1000;
}

/** Rounded spatial cache key: it intentionally does not include user identity or exact coordinates. */
export function nearbyPlaceCacheKey(lat: number, lon: number, radiusMeters: number): string {
  return `${roundForNearbyQuery(lat).toFixed(3)}:${roundForNearbyQuery(lon).toFixed(3)}:${Math.round(radiusMeters)}`
    .replace(/[^0-9:.-]/g, "");
}

export function buildNearbyOverpassQuery(lat: number, lon: number, radiusMeters: number): string {
  const safeLat = roundForNearbyQuery(lat).toFixed(3);
  const safeLon = roundForNearbyQuery(lon).toFixed(3);
  const radius = Math.round(Math.max(50, Math.min(500, radiusMeters)));
  const clauses = [
    `nwr(around:${radius},${safeLat},${safeLon})[name][historic]`,
    `nwr(around:${radius},${safeLat},${safeLon})[name][tourism~"attraction|museum|viewpoint|artwork|gallery|zoo|information"]`,
    `nwr(around:${radius},${safeLat},${safeLon})[name][amenity~"restaurant|cafe|library|theatre|arts_centre|place_of_worship|marketplace"]`,
    `nwr(around:${radius},${safeLat},${safeLon})[name][natural~"peak|waterfall|beach|cave|bay|cliff"]`,
    `nwr(around:${radius},${safeLat},${safeLon})[name][leisure~"park|garden|nature_reserve"]`,
    `nwr(around:${radius},${safeLat},${safeLon})[name][man_made~"bridge|tower|lighthouse|water_tower"]`,
  ];
  return `[out:json][timeout:8];(\n${clauses.join(";\n")};\n);out center tags;`;
}

function record(value: unknown): Record<string, unknown> {
  return value !== null && typeof value === "object" && !Array.isArray(value)
    ? value as Record<string, unknown>
    : {};
}

function pointForElement(element: Record<string, unknown>): { lat: number; lon: number } | null {
  const center = record(element.center);
  const lat = Number(element.lat ?? center.lat);
  const lon = Number(element.lon ?? center.lon);
  return Number.isFinite(lat) && Number.isFinite(lon) && Math.abs(lat) <= 90 && Math.abs(lon) <= 180
    ? { lat, lon }
    : null;
}

export function haversineMeters(a: { lat: number; lon: number }, b: { lat: number; lon: number }): number {
  const radians = (degrees: number) => degrees * Math.PI / 180;
  const dLat = radians(b.lat - a.lat);
  const dLon = radians(b.lon - a.lon);
  const lat1 = radians(a.lat);
  const lat2 = radians(b.lat);
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) ** 2;
  return 6371000 * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(Math.max(0, 1 - h)));
}

export function bearingDegrees(a: { lat: number; lon: number }, b: { lat: number; lon: number }): number {
  const radians = (degrees: number) => degrees * Math.PI / 180;
  const y = Math.sin(radians(b.lon - a.lon)) * Math.cos(radians(b.lat));
  const x = Math.cos(radians(a.lat)) * Math.sin(radians(b.lat)) -
    Math.sin(radians(a.lat)) * Math.cos(radians(b.lat)) * Math.cos(radians(b.lon - a.lon));
  return (Math.atan2(y, x) * 180 / Math.PI + 360) % 360;
}

function relativeHeading(headingDeg: number, bearingDeg: number): number {
  return ((bearingDeg - headingDeg + 540) % 360) - 180;
}

function categoryFor(tags: Record<string, unknown>): string {
  for (const key of CATEGORY_KEYS) {
    const value = tags[key];
    if (typeof value === "string" && value.trim()) {
      return `${key.replace(/_/g, " ")}: ${value.replace(/_/g, " ")}`;
    }
  }
  return "mapped place";
}

export function normalizeNearbyPlaces(payload: unknown, options: NearbyPlaceOptions): NearbyAdventurePlace[] {
  const data = record(payload);
  const rawElements = Array.isArray(data.elements) ? data.elements.slice(0, 80) : [];
  const origin = { lat: roundForNearbyQuery(options.lat), lon: roundForNearbyQuery(options.lon) };
  const radius = Math.max(50, Math.min(500, options.radiusMeters ?? 250));
  const headingReliable = Number.isFinite(options.headingDeg) && (options.speedMps ?? 0) >= 0.45;
  const seen = new Set<string>();
  const candidates: Array<NearbyAdventurePlace & { score: number }> = [];

  for (const raw of rawElements) {
    const element = record(raw);
    const tags = record(element.tags);
    const name = typeof tags.name === "string" ? tags.name.trim().replace(/\s+/g, " ").slice(0, 100) : "";
    const point = pointForElement(element);
    const type = typeof element.type === "string" ? element.type : "";
    const osmId = Number(element.id);
    if (!name || !point || !["node", "way", "relation"].includes(type) || !Number.isSafeInteger(osmId) || osmId <= 0) continue;

    const id = `${type}/${osmId}`;
    const dedupeKey = `${name.toLocaleLowerCase("en-US")}:${Math.round(point.lat * 10000)}:${Math.round(point.lon * 10000)}`;
    if (seen.has(dedupeKey)) continue;
    seen.add(dedupeKey);

    const distanceMeters = Math.round(haversineMeters(origin, point));
    if (distanceMeters > radius) continue;

    const bearingDeg = Math.round(bearingDegrees(origin, point));
    const relativeAngle = headingReliable ? relativeHeading(options.headingDeg!, bearingDeg) : 0;
    const clockIndex = headingReliable ? Math.round(((relativeAngle + 360) % 360) / 30) % 12 : -1;
    const relativeClock = clockIndex >= 0 ? CARDINAL_CLOCK[clockIndex] : undefined;
    const relativeDirection = !headingReliable ? undefined
      : Math.abs(relativeAngle) < 45 ? "ahead"
      : Math.abs(relativeAngle) > 135 ? "behind"
      : relativeAngle < 0 ? "left" : "right";
    const score = (headingReliable ? 2.4 * Math.abs(relativeAngle) : 0) + 0.45 * distanceMeters;

    candidates.push({
      id,
      name,
      category: categoryFor(tags),
      lat: Math.round(point.lat * 100000) / 100000,
      lon: Math.round(point.lon * 100000) / 100000,
      distanceMeters,
      bearingDeg,
      ...(relativeClock ? { relativeClock } : {}),
      ...(relativeDirection ? { relativeDirection } : {}),
      mapUrl: `https://www.openstreetmap.org/${id}`,
      score,
    });
  }

  return candidates
    .sort((a, b) => a.score - b.score || a.distanceMeters - b.distanceMeters)
    .slice(0, 8)
    .map(({ score: _score, ...place }) => place);
}
