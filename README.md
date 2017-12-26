# ElectricFieldSim
by Orion Forowycz

Java program for simulating electric fields around source charges.

Downloads are in .jar files in the releases tab on Github.

# Instructions

Field lines will come out of positive source charges and will eventually head towards negative source charges until they reach their draw limit or a negative source charge.

## Controls:
###### (all controls ignore the shift key)
* Left click to place a positive charge.
* Right click to place a negative charge.
* WASD or arrow keys to pan/move.
* Q or + to zoom in.
* E or - to zoom out.

## Viewer Options Window:
* Charge determines the charge of source charges placed by left or right clicking. This will not change past placed source charges.
* Draw Limit determines how far the lines will draw. A larger value draws more of the field but takes more time.
* Step determines the length of the pieces that make up each field line. A smaller value will generally make the field lines smoother at the cost of how far they will go.
* Scale determines how many lines come out of each positive source charge, if dynamic scaling is off.
* Dynamic Scale makes the number of lines that come out of each positive source charge equal to this number times the charge of the positive source charge, if dynamic scaling is on.
* The Dynamic Scale option turns dynamic scaling on or off.
* The Refresh button refreshes the electric field and source charge drawings, usually done automatically
* The Clear All button clears all the source charges and their fields from the viewer
