# MRX-LOGIN 1.0.0 feature status

Status key: **Implemented** = code path exists; **Unit tested** = automated test exists; **Runtime unverified** = requires a live Minecraft server. **Not implemented** = do not enable/use.

| Area | Feature | Status |
|---|---|---|
| Authentication | Register/login/logout/change password | Implemented; runtime unverified |
| Authentication | PBKDF2-HMAC-SHA256 with random salt | Implemented; unit tested |
| Authentication | Premium auto-login | Not implemented |
| Authentication | Session login | Not implemented |
| Authentication | Bedrock/Floodgate | Not implemented |
| Authentication | TOTP 2FA | Not implemented |
| Password | Min/max length | Implemented |
| Password | Basic weak-password denylist | Implemented |
| Password | Password input hidden from command logs | Not implemented; command arguments remain a known risk |
| Registration | Confirmation option | Implemented |
| Registration | Maximum accounts per IP | Implemented using registration IP; proxy-aware behavior unverified |
| Protection | Failed login counter / temporary lockout | Implemented in memory; resets on restart |
| Protection | Login timeout | Implemented |
| Protection | Movement/block/chat/command restrictions | Implemented |
| Protection | Inventory/interact/damage/drop restrictions | Implemented |
| Protection | Captcha / AntiBot | Not implemented |
| Spawn | First-join spawn | Implemented; runtime unverified |
| Spawn | Save last location | Implemented; runtime unverified |
| Spawn | Restore last location / portal / void fixes | Not implemented |
| Email | Resend/Brevo provider helper | Partial helper only; no user workflow |
| Email | Verification/recovery/resend/cooldown | Not implemented |
| Discord | Verification | Not implemented |
| Admin | `/ml`, help, reload, info, version, status, debug | Implemented |
| Admin | Unregister account | Implemented |
| Admin | Full admin account/session/backup/migration suite | Not implemented |
| Database | SQLite | Implemented; runtime unverified |
| Database | MySQL/MariaDB/PostgreSQL/H2 | Not implemented |
| Database | Backup/migration/purge | Not implemented |
| Proxy | Velocity/BungeeCord | Not implemented |
| Runtime | Folia scheduler adapter | Not implemented |
| Compatibility | Paper API 1.21.11 / Java 21 | Build target; CI/runtime must verify |
| Compatibility | Other Paper versions, Spigot, Bukkit, Purpur | Not verified |
| Compatibility | Beta/Snapshot/future versions | Not verified |

## Release gate

The GitHub workflow must pass compilation, unit tests and packaged-resource checks before creating a release. Even after CI passes, a live Paper 1.21.11 staging-server test is required before production approval.
