import json
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timedelta, timezone

ENDPOINT = "https://data.bafu.admin.ch/api"
STATIONS = ("2091", "2289")


def post(query, variables=None):
    payload = json.dumps({"query": query, "variables": variables or {}}).encode("utf-8")
    request = urllib.request.Request(
        ENDPOINT,
        data=payload,
        method="POST",
        headers={"Content-Type": "application/json", "Accept": "application/json", "User-Agent": "PFVR-CI/0.9.0"},
    )
    with urllib.request.urlopen(request, timeout=20) as response:
        return response.status, json.loads(response.read().decode("utf-8"))


def retry(query, variables=None):
    last = None
    for attempt in range(3):
        try:
            return post(query, variables)
        except urllib.error.HTTPError as exc:
            body = exc.read().decode("utf-8", errors="replace")
            last = RuntimeError(f"HTTP {exc.code}: {body[:500]}")
            # Client errors indicate our query/request is wrong; do not hide them.
            if 400 <= exc.code < 500:
                raise last
        except Exception as exc:
            last = exc
        if attempt < 2:
            time.sleep(2)
    raise last or RuntimeError("BAFU probe failed")


station_query = """
query Station($station: String!) {
  water {
    observations {
      stations(where: { no: { _eq: $station } }, limit: 1) {
        no
        name
        riverName
      }
    }
  }
}
"""

hourly_query = """
query Hourly($station: String!, $from: AWSDateTime!, $to: AWSDateTime!) {
  water {
    observations {
      data_1hour_mean(
        where: {
          station: { no: { _eq: $station } }
          timestamp: { _gte: $from, _lt: $to }
        }
      ) {
        parameterName
        timestamp
        value
        unitSymbol
      }
    }
  }
}
"""

live_query = """
query Live($station: String!) {
  water {
    observations {
      data_live(where: { stationNo: { _eq: $station } }) {
        stationNo
        parameterName
        timestamp
        value
        releaseStatus
      }
    }
  }
}
"""

now = datetime.now(timezone.utc)
start = now - timedelta(hours=8)
variables_window = {
    "from": start.isoformat().replace("+00:00", "Z"),
    "to": now.isoformat().replace("+00:00", "Z"),
}

try:
    for station in STATIONS:
        _, metadata = retry(station_query, {"station": station})
        if metadata.get("errors"):
            raise RuntimeError(f"Station {station} metadata errors: {metadata['errors']}")
        stations = metadata["data"]["water"]["observations"]["stations"]
        if not stations or stations[0].get("no") != station:
            raise RuntimeError(f"Station {station} metadata not found")

        variables = {"station": station, **variables_window}
        _, hourly = retry(hourly_query, variables)
        if hourly.get("errors"):
            raise RuntimeError(f"Station {station} hourly errors: {hourly['errors']}")
        rows = hourly["data"]["water"]["observations"]["data_1hour_mean"]
        if not rows:
            raise RuntimeError(f"Station {station} returned no hourly observations")
        if not any(row.get("parameterName") == "Q" for row in rows):
            raise RuntimeError(f"Station {station} hourly data lacks Q")

        _, live = retry(live_query, {"station": station})
        if live.get("errors"):
            raise RuntimeError(f"Station {station} live errors: {live['errors']}")
        live_rows = live["data"]["water"]["observations"]["data_live"]
        if not live_rows:
            raise RuntimeError(f"Station {station} returned no live observations")
        print(f"BAFU {station}: {stations[0].get('name')} · {len(rows)} hourly · {len(live_rows)} live rows")
except Exception as exc:
    print(f"BAFU probe failed: {exc}", file=sys.stderr)
    sys.exit(1)
