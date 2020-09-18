# ChunkClaimFabric
ChunkClaimFabric is a derivative of boformer's ChunkClaim Bukkit mod for 
Minecraft servers. This software is a port to the Fabric mod loader. 
The original can be found here: https://github.com/boformer/ChunkClaim.

# Commands
## Normal Commands
- `/chunk` 
  - Base command that prints information about the current chunk.
  - Normal information:
    - If the chunk is public.
    - Else,
      - If the command is said by the claim owner:
        - The list of builders, if any.
      - Else, whether the player that said the command is a builder.
- `/chunk abandon`
  - Allows claim owners to revoke claim ownership of the current chunk and get a refund on the chunk
  cost.
- `/chunk claim`
  - Explicitly claims the current chunk and removes the configured amount of credits.
- `/chunk credits`
  - Displays the current amount of credits a player has, the chunk claim price, and the mob spawn
  price.
- `/chunk help`
  - Prints out usage prompts for all Chunk Claim commands.
- `/chunk trust <player name>`
  - Example: `/chunk trust Player123`
  - Adds the target player to the list of trusted builders for all of your chunk claims. This
  allows them to build in all the owner's chunk claims.
- `/chunk untrust <player name>`
  - Example: `/chunk trust Player123`
  - Removes the target player from the list of trusted builders for all of your chunk claims. This
  prevents them from building in all the owner's chunk claims.
## Admin Commands
Additional commands usable by players that have the `"mod"` permission.
- `/chunk` 
  - Base command that prints information about the current chunk.
  - Additional mod information:
    - Chunk ID used in datastore.
    - Permanent (Based on modifiedBlocks?)
    - Last login of chunk owner.
    - List of trusted builders.
    - Visualization of the chunk.
- `/chunk bonus <player name> <amount>`
  - Example: `/chunk bonus Player123 5`
  - Gives the target player bonus credits for buying chunk claims or mob spawns.
- `/chunk delete <player name> <radius>`
  - Example: `/chunk delete Player123 5`
  - Deletes the target player's chunk claims within the given radius.
  - The radius can be unspecified, meaning delete the current chunk claim.
  - Deletion performs the same operation as abandon.
- `/chunk deleteall <player name>`
  - Example: `/chunk deleteall Player123`
  - Deletes ALL the target players chunk claims.
- `/chunk ignore`
  - Allows the player to ignore chunk claims.
  - Ignore permission cannot override:
    - Tree spread between claims.
    - Grass spread between claims.
    - Fluid flow between claims
    - Dispensing between claims.
- `/chunk next <player name>`
  - Example: `/chunk next Player123`
  - Goes to the next chunk owned by the target player.


# Configuration
The configuration file must be in `<your server game folder>/config/chunkclaimfabric.json5`. 
The file or any missing field within it will be automatically generated with defaults if it does 
not exist. 

Default configuration:
```json5
{ 
  // List of world names configured for chunk claim.
  "worlds": [ 
    "ExampleWorldName"
  ],
  /* Configures the number of days before a claimed chunk that has not met the minimum amount of modified blocks is to be automatically deleted.
     A value of zero disables this feature.
     See related configuration: minModBlocks
  */
  "autoDeleteDays": 0.0,
  // Credits price to claim a single chunk. A price of zero is valid.
  "chunkPrice": 1.0,
  // Number of credits acquired per hour of continuous and non-idle play time.
  "creditsPerHour": 1.0,
  // Maximum number of credits a player can have.
  "maxCredits": 1.7976931348623157E308,
  /* Minimum number of blocks that have to be modified in a chunk for it to
     be considered "built" upon.
     See related configuration: autoDeleteDays
  */
  "minModBlocks": 0,
  /* If true, players are allowed to use spawn eggs in their claimed chunks at a credits price.
     Else if false, spawn egg usage is not allowed anywhere in chunk claim protected worlds.
  */
  "mobsForCredits": true,
  // Credit price to use spawn eggs. A price of zero is valid.
  "mobPrice": 0.0,
  /* If true, players are required to claim chunks next to their existing claims.
     Else if false, players can claim chunks wherever they want.
  */
  "nextToForce": false,
    /* If true, players are not allowed to access containers unless they are trusted in the claim that the container resides in.
       Else if false, players are allowed to access any container in any chunk regardless of permissions.
       A container is any block with usable storage slots.
    */
  "protectContainers": true,
  /* If true, players are not allowed to use redstone switches (levers or buttons) unless they are trusted in the claim that the switch resides in.
     Else if false, players are allowed to use any switch in any chunk regardless of permissions.
  */
  "protectSwitches": true,
  /* If true, chunks are regenerated to their naturally generated state when abandoned or deleted.
     Else if false, any modifications are kept when chunks are abandoned or deleted.
  */
  "regenerateChunk": true,
  // The amount of credits a player is given when they join the server for the first time.
  "startCredits": 1.0
}
```
