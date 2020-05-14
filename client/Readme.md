# Readme
This readme consists of all knowledge we gathered about the TimeFlip cube.

## Information gathering
If we want to get some information about the facets, or the history we need to input the password.
The characteristics ID for the password is `F1196F57-71A4-11E6-BDF4-0800200C9A66`. 

To get information about the current facet we get from the characteristics ID: `F1196F52-71A4-11E6-BDF4-0800200C9A66`.
Other characteristics we use are the command result output characteristic: `F1196F53-71A4-11E6-BDF4-0800200C9A66` and
the Command characteristic: `F1196F54-71A4-11E6-BDF4-0800200C9A66`

We send and receive information in hex.

## Everything about the History
### History read out 
If we want to read out the history we need to send the command `0x01` to the command characteristics and then read out the result of the command result output characteristic.

### History delete
If we want to delete the current history we need to send the command `0x02` to the command characteristics. 

### History decode
We get the history entrys in pairs of 7. 
To get all history entrys we need to read out the command result output characteristic until we get a line containing only `0x00`.
The line before the `0x00` contains the times we read the history. 

Example:

`{0x0d 0x00 0x18 0x3c 0x00 0x04 0x10 0x00 0x04 0x10 0x00 0x08 0x16 0x00 0x0c 0x32 0x00 0x10 0x0d 0x00 0x14}` -> history line

`{0x0d 0x00 0x18 0x3c 0x00 0x04 0x10 0x00 0x04 0x10 0x00 0x08 0x16 0x00 0x0c 0x32 0x00 0x10 0x0d 0x00 0x14}` -> another history line

`{0x0f 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00}` -> number of history read outs

`{0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00}` -> number of history read outs

Each history entry consists of 3 bytes, which are decoded in Little Endian. 
This means we have to switch byte 1 and 3. 
After this is done we can take the first 6 bits which resemble the facet ID and the other bits which resemble the seconds the cube was on this facet.

Example: Lets take following history line

`{0x0d 0x00 0x18 0x3c 0x00 0x04 0x10 0x00 0x04 0x10 0x00 0x08 0x16 0x00 0x0c 0x32 0x00 0x10 0x0d 0x00 0x14}`

The first history entry would be `0x0d 0x00 0x18`, the second `0x3c 0x00 0x04`, ...

If we want to decode for example `0x0d 0x00 0x18` we need to switch `0x0d` and `0x18` -> `0x18 0x00 0x0d`.

Now we take the first six bits of `0x18 0x00 0x0d` which are `000110` = `6` and the rest `000000000000001101` = `13`

This means our history entry lied on facet `6` for `13` seconds.


## Battery outage
If the battery is removed or is empty the TimeFlip resets his configuration, such as the Password and the Facet ID's.
This is a big problem because in our server we have assigned facet ID's to activities.

Therefore we examined how the facets get the new ID's:
* The facet which is on top during the first read out after the reset gets facet ID number one.
* The ID two gets the second facet 
* The ID three gets the third facet
* ...

With this knowledge we can manipulate the facet ID assignment such that we get always ID's from 1-12.

## Other things
Some strange behaviours:
* Sometimes the characteristics or the services aren't available. 
* Sometimes the TimeFlip disconnects without any reason. 
* The distance from the TimeFlip to the Raspberry PI varies. If you need a somewhat reliable connection you should stay under 2 Meters.

## Some more Things
* We can read out Services and Characteristics of the cube even if we don't are connected to it.
* The function `.find()` would have been helpful for the Services and characteristics. 

