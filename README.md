# ClientBase
### Tested on minecraft 1.8.9
Known issues:
- `net.minecraft.world.gen.structure.StructureMineshaft.Pieces:line611`
  ```diff
  -for (lvt_5_1_ = 0; ...
  +for (k = 0; ...
  ```

## Disclaimer
This repo contains a patched version of MCP 918 (mcp.zip) the credits for the original code go to the original authors. The code is provided as is and without any warranty. The author is not responsible for any damage caused by the code.
Mojang owns the Minecraft name, brand and assets.
This project nor the author is affiliated with Mojang in any way.
Please follow the [Mojang EULA](https://account.mojang.com/documents/minecraft_eula) and the [Mojang Terms of Service](https://account.mojang.com/terms).


## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details


## How to use?
1. clone the repo
2. set the name of the client in `settings.gradle.kts`  line 1: `rootProject.name = "<name>" // e.g. "ClientBase"`
3. run `./gradlew setupWorkspace` to set up the workspace (decompiles the code, ...)
4. do your changes
5. run `./gradlew runClient` to run the modified client (debug...)
6. fix your changes
7. run `./gradlew assembleClient` to build the modified jar
8. enjoy the modified client: `build/libs/<name>.jar` and `build/libs/<name>.json`


## Can I use another minecraft version?
Yes, you can use any version that you can find the mcp.zip for. 
Copy the mcp.zip into the root directory of the repo and set the
version and the mcp zip file in `build.gradle.kts`:
```kotlin
/* line:1 */ ext["mcClientVersion"] = "<version>" // e.g. "1.8.9"
/* line:2 */ ext["mcpZip"] = "<mcp zip file>" // e.g. "mcp.zip"
```

## Gradle specifications
If you want to add jars as dependencies, don't put them into the `libs` folder (might cause problems with the build script).
If you want a dependency to be included in the final jar use the `bundled` configuration:
```kotlin
dependencies {
    bundled("com.example:example:1.0.0") // implementation("...") + bundled in jar
}
```
