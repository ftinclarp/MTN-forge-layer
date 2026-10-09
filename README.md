# MTN-forge-layer

Runtime shim for Forge 1.7.10 mods ported by Mod-Transmuder-Next.
Not a standalone mod.

Provides dummy implementations of the Forge 1.7.10 API
(`mtn.forge_layer.cpw.mods.fml.*`) so a ported mod's source compiles,
plus a Fabric entry point (`mtn.forge_layer.FabricEntry`) that finds
the `@Mod`-annotated class and dispatches to it via reflection
(preInit -> init -> postInit), injecting `@SidedProxy` instances.

## Usage as a dependency

Via JitPack:

    repositories {
        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        implementation("com.github.ftinclarp:MTN-forge-layer:v1.0.0")
    }

Via local publish:

    ./gradlew publishToMavenLocal

Then depend on com.github.ftinclarp:MTN-forge-layer:1.0.0.

## License

MIT. See [LICENSE](LICENSE).
