# ChunkClaimFabric
ChunkClaimFabric is a derivative of boformer's ChunkClaim Bukkit mod for 
Minecraft servers. This software is a port to the Fabric mod loader. 
The original can be found here: https://github.com/boformer/ChunkClaim.

# Commands

 - `/chunk` Base command that prints information about the current chunk.
   - Normal information:
     - If the chunk is public.
     - Else,
       - If the command is said by the claim owner:
         - The list of builders, if any.
       - Whether the player that said the command is a builder.
   - Admin information:
     - Chunk ID used in datastore.
     - Permanent (Based on modifiedBlocks?)
     - Last login of chunk owner.
     - List of trusted builders.
     - Visualization of the chunk.
- `/chunk abandon` 

# Configuration
The configuration file must be in `<your game folder>/config/chunkclaimfabric.json5`. The file or 
any missing field within it will be automatically generated with defaults if it does not exist. 

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

# Ignore Permission
- Ignore permission cannot override:
  - Tree spread between claims.
  - Grass spread between claims.
  - Fluid flow between claims
  - Dispensing between claims.

# License Templates
## Modified Files:
```
/*
    ChunkClaim Plugin for Minecraft Bukkit Servers
    Copyright (C) 2012 Felix Schmidt
    Copyright (C) 2020 PlateOfPasta: Notice of modification for ChunkClaimFabric
    
    This file is part of ChunkClaim.
    
    ChunkClaim is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    ChunkClaim is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with ChunkClaim.  If not, see <http://www.gnu.org/licenses/>.
 */
```
## New Files
```
/*
    ChunkClaim Plugin for Minecraft Fabric Servers
    Copyright (C) 2020 PlateOfPasta
    
    This file is part of ChunkClaim and derivative work ChunkClaimFabric.
    
    ChunkClaim is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    ChunkClaim is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with ChunkClaim.  If not, see <http://www.gnu.org/licenses/>.
 */
```