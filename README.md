# BountyHunt

Advanced Player Bounty System for Paper 1.21+.

## Features

* **Live Wanted Status:** Players with bounties have BossBars and particle effects.
* **Reputation System:** Earn ranks based on bounty kills.
* **GUI System:** Visual menu for active bounties.
* **Vault Integration:** Place and earn bounties using server economy.

## Commands

* `/bounty` - Opens the active bounties GUI.
* `/bounty set <player> <amount>` - Set a bounty on a player.
* `/bounty track <player>` - Point your compass at where a wanted player is now and show the distance.
* `/bounty reload` - Reload configurations.

## Permissions

* `bountyhunt.use` - Basic commands.
* `bountyhunt.admin` - Admin commands.

## Placeholders (PlaceholderAPI)

* `%bountyhunt_bounty%` - Current bounty on the player.
* `%bountyhunt_reputation%` - Player's reputation score.
* `%bountyhunt_rank%` - Player's hunter rank.
