# JMWaypointsToXaero

<p align="center">
  <a href="https://discord.gg/nJZrSaRKtb">
  <img alt="Discord" src="https://dcbadge.limes.pink/api/server/nJZrSaRKtb">
  </a>
</p>

Convert JourneyMap Waypoints to Xaero Waypoints

# Warning

This will overwrite any Xaero Waypoints you have defined already for your selected server being converted.

# Usage

Download the latest jar from releases: https://github.com/rfresh2/JMWaypointsToXaero/releases/latest

## Option 1: GUI

Double click the jar to open the GUI

## Option 2: CLI

`java -jar JMWaypointstoXaero-1.3.jar <input folder> <output folder>`

## Inputs

The Input folder is your JourneyMap directory for your selected world.

Example:
`C:\Users\rfresh2\AppData\Roaming\.minecraft\journeymap\data\mp\2b2t`

Output folder is your destination Xaero Waypoints directory

Example:
`C:\Users\rfresh2\AppData\Roaming\.minecraft\xaero\minimap\Multiplayer_2b2t.org`

# Full CLI example

`java -jar JMWaypointsToXaero-1.3.jar "C:\Users\rfresh2\AppData\Roaming\.minecraft\journeymap\data\mp\2b2t" "C:\Users\rfresh2\AppData\Roaming\.minecraft\xaero\minimap\Multiplayer_2b2t.org"`

