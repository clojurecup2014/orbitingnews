#!/bin/sh
lein uberjar && scp target/orbitingnews-0.1.0-SNAPSHOT-standalone.jar cloudsigma@31.171.245.197:~ && ssh cloudsigma@31.171.245.197
