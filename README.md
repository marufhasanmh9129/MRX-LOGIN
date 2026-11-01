# MRX-LOGIN 1.0.0

**Name:** MRX-LOGIN  
**Output file:** `MRX-LOGIN.jar`  
**Version:** `1.0.0`  
**Author:** MRX DEVELOPMENT  
**License:** MIT  
**Build target:** Paper API `1.21.11-R0.1-SNAPSHOT`  
**Java:** 21

> **Release scope:** This is a Paper 1.21.11 authentication release, not a universal Bukkit/Spigot/Folia/Velocity/BungeeCord plugin. It does not implement every feature in the original 43-item wish list. Unsupported settings are rejected at startup rather than silently pretending to work. Do not use it as a production authentication gateway until you have tested it on a staging server.

## Build from GitHub

1. Upload the entire repository, including `.github/workflows/build.yml`, to GitHub.
2. Open **Actions** and run **Build MRX-LOGIN**, or push to `main`/`master`.
3. The workflow uses JDK 21 and runs `mvn clean verify`.
4. On success, download the `MRX-LOGIN` artifact. It contains `MRX-LOGIN.jar`.
5. To publish a GitHub Release, create and push a tag: `git tag v1.0.0 && git push origin v1.0.0`.

A successful CI build proves compilation and unit tests for the configured Paper API. It does not prove runtime behavior on every server fork or version.

## Implemented in this release

- Paper plugin bootstrap and metadata
- `/register`, `/login`, `/logout`, `/changepassword`
- SQLite account storage with prepared statements
- PBKDF2-HMAC-SHA256 password hashing, random per-password salt and constant-time comparison
- Password length checks and a small built-in weak-password denylist
- Optional registration confirmation
- Maximum accounts per IP enforcement at registration
- Failed-login counter and in-memory temporary lockout
- Login timeout
- Unauthenticated movement, block break/place, inventory, interaction, damage, drop, chat and command restrictions
- Optional first-join spawn
- Last-location coordinate saving (automatic restore is not implemented)
- `config.yml` validation and separate `messages.yml`
- `/ml`, `/ml help`, `/ml reload`, `/ml info`, `/ml version`, `/ml status`, `/ml debug`
- `/ml admin unregister <player>`
- GitHub Actions build, tests and JAR artifact/release workflow

## Explicitly not implemented in 1.0.0

- Pre-backend login GUI / proxy limbo
- In-game inventory GUI authentication
- Premium auto-login / trusted proxy identity validation
- Session login
- Floodgate / Bedrock auto-login
- TOTP 2FA
- Email verification, recovery and `/email` commands (provider helper code is not a complete workflow)
- Discord verification
- Captcha and AntiBot
- Persistent IP bans / persistent login-attempt lockout
- Automatic last-location restore and portal/void fixes
- MySQL, MariaDB, PostgreSQL and H2 adapters
- Database backup, migration and purge
- Velocity and BungeeCord modules
- Folia scheduler adapter
- Compatibility with old Minecraft versions, Beta or Snapshots
- Complete admin account management beyond unregister
- Event-driven command automation, configurable titles/sounds/action bars

The `ConfigManager` refuses to start if a currently unsupported module is enabled or if a non-SQLite database is selected. This prevents no-op configuration from being mistaken for a working feature.

## Commands

| Command | Purpose |
|---|---|
| `/register <password> <confirm>` | Create an account (confirmation can be disabled in config) |
| `/login <password>` | Authenticate |
| `/logout` | End the current authenticated session |
| `/changepassword <old> <new>` | Change password while authenticated |
| `/ml help` | Show administration help |
| `/ml reload` | Reload and validate configuration/messages |
| `/ml info` / `/ml version` | Plugin information |
| `/ml status` | Current mode and online count |
| `/ml debug` | Show debug setting |
| `/ml admin unregister <player>` | Delete an account |

## Configuration

- `plugins/MRX-LOGIN/config.yml`: plugin settings.
- `plugins/MRX-LOGIN/messages.yml`: player/admin-facing messages.
- `plugins/MRX-LOGIN/accounts.db`: SQLite account database.

API keys must be configured as environment variables on each server host. Never commit secrets to GitHub. A plugin cannot conceal secrets from the machine administrator.

## Security notes and known limitations

- Bukkit command arguments can be captured by server command logs or third-party logging plugins. This release still accepts passwords as command arguments; use only in a trusted environment and review command logging. A secure chat-prompt credential flow is not included.
- SQLite queries and PBKDF2 are currently synchronous in command/event paths. High concurrency may cause tick delays. This release is not load-tested.
- Failed-attempt counters and temporary lockouts are in-memory and reset on server restart. They are keyed by player UUID, not shared across a proxy network.
- IP account limits use the IP recorded at registration; proxy deployments require correct trusted forwarding and are not supported by this release.
- The Paper API target and `api-version` are intentionally specific. No universal-version claim is made.
- No release can guarantee zero bugs. Run on a staging server, back up data, and verify server logs before production use.

## Tests

JUnit tests cover password hashing, verification, salt uniqueness and malformed hash rejection. GitHub Actions runs these tests during `mvn clean verify`. Runtime server integration tests and platform compatibility tests are not included yet.

## License

MIT. See [LICENSE](LICENSE).
