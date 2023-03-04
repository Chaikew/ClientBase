# ClientBase
### Tested on minecraft 1.8.9


## Disclaimer
This repo contains a patched version of MCP 918 (mcp.zip) the credits for the original code go to the original authors. This repo is only for educational purposes and should not be used for commercial purposes. The code is provided as is and without any warranty. The author is not responsible for any damage caused by the code.
Mojang owns the Minecraft name, brand and assets.
This project nor the author is affiliated with Mojang in any way.
Please follow the [Mojang EULA](https://account.mojang.com/documents/minecraft_eula) and the [Mojang Terms of Service](https://account.mojang.com/terms).


## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details


## How to use?
1. clone the repo
2. set the name of the client in `settings.gradle.kts`  line 1: `rootProject.name = "<name>" // e.g. "ClientBase"`
3. run `./gradlew setupWorkspace` to setup the workspace (decompiles the code, ...)
4. do your changes
5. run `./gradlew assembleClient` to build the modified jar
6. enjoy the modified client: `build/libs/<name>.jar` and `build/libs/<name>.json`


## Can I use another minecraft version?
Yes, you can use any version that you can find the mcp.zip for. 
Copy the mcp.zip into the root directory of the repo and set the
version and the mcp zip file in `settings.gradle.kts`:
```kotlin
ext["mcClientVersion"] = "<version>" // e.g. "1.8.9"
ext["mcpZip"] = "<mcp zip file>" // e.g. "mcp.zip"
```
