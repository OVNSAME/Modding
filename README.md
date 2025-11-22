# Modding
Modding is a very lightweight Java library that adds new classes, interfaces and enumerations for full work with Minecraft's integrations from popular publishing platforms.

### Main features

You can add this project in your dependencies using JitPack.

```groovy
repositories {
    maven {
        url "https://jitpack.io"
    }
}

dependencies {
    implementation 'com.github.OVNSAME:Modding:1.4.0'
}
```

Currently, it embraces three main platforms:
 - CurseForge
 - Modrinth
 - Spigot

Method ```getIntegration``` from class ```Modding``` allows you to get any integration from any supported platform.

```java
import java.io.IOException;
import net.ovonsame.modding.Modding;
import net.ovonsame.modding.interfaces.Integration;
import net.ovonsame.modding.enumeration.Platform;

public class Overview {
    public static void main(String[] args) {
        try{
            Integration integration1 = Modding.getIntegration(Platform.CURSEFORGE, "123456", "key");
            
            Integration integration2 = Modding.getIntegration(Platform.MODRINTH, "123456", null);
            
            Integration integration3 = Modding.getIntegration(Platform.SPIGOT, "123456", null);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
```

Every platform has its own peculiarities and differences between others.
Some may require API key in the method, like CurseForge. However, if platform does not require key, you can put ```null``` as the key. If key is not provided and the platform requires it, method will throw an exception.
Also, some methods of Integration interface may return null or empty list because the platform does not provide info needed for the method. So be careful when using them and look at the annotations.

In order not to load the integration immediately, but only to indicate the main criteria, the record class ```LazyIntegration``` is used. Platform and identifier are required in a constructor of the class. You can also use it in ```getIntegration``` method.
More than that you can get lazy integrations as static fields of ```Integrations``` interface. There are listed popular mods and plugins from the platforms.

### Integration and Types

Library can work with any type of Minecraft's integrations, not only mods.
In the list there are:
 - Mod
 - Modpack
 - Plugin
 - Resourcepack
 - Datapack
 - Shader
 - Customization
 - Addon
 - World

All of them are listed in ```IntegrationType``` enumeration.

### Integration data and Enums

Every integration type has its loaders, categories, status and edition.

There are three enums for loaders:
 - ```ModLoader```
 - ```PluginLoader```
 - ```ShaderLoader```

By default, other types have ```ILoader.ANY``` loader. And ```ILoader``` is a generalizing interface for all loaders.

The generalizing interface for categories is ```ICategory```.
There are all enumerations of possible categories:
 - ```ModCategory```
 - ```PluginCategory```
 - ```ShaderCategory```
 - ```CustomizationCategory```
 - ```AddonCategory```
 - ```WorldCategory```
 - ```DatapackCategory```
 - ```ResourcepackCategory```
 - ```ModpackCategory```

All these categories have ```ICategory.CURSED``` category as standart because lots of categories from Modrinth has the "Cursed" tag, so to lessen the amount of values that category was added.

Integration status is represented by ```IntegrationStatus``` enumeration and can be only one in the integration.

And the edition is represented by ```Edition``` enumeration. It contains only two values: ```JAVA``` and ```BEDROCK```.

### Interfaces and methods

In an example below all methods of ```Integration``` interface are shown.

```java
import java.io.PrintStream;
import java.util.Arrays;
import java.io.IOException;

import net.ovonsame.modding.Modding;
import net.ovonsame.modding.interfaces.Integration;
import net.ovonsame.modding.enumeration.Platform;

public class Overview {
    public static void main(String[] args) {
        try {
            Integration i = Modding.getIntegration(Platform.CURSEFORGE, "238222", "key");
            PrintStream console = System.out;

            console.println(i.getTitle());
            console.println(i.getSlug());
            console.println(i.getFullDescription());
            console.println(i.getId);
            console.println(i.getLicense());

            console.println(i.getOrganisation.getName());
            console.println(i.getTeam().getName());
            console.println(i.getOrganisation().getOwner().getName());

            console.println(Arrays.toString(i.getAuthors()));

            console.println(i.getPublished());
            console.println(i.getUpdated());
            console.println(i.getApproved());

            console.println(i.getType());

            console.println(Arrays.toString(i.getCategories()));

            console.println(i.getStatus());
            
            console.println(i.getEdition());

            console.println(i.getDownloads());
            console.println(i.getLikes());
            console.println(i.isPremium());

            if (i.getIcon() != null) console.println(i.getIcon());
            if (i.getIssues() != null) console.println(i.getIssues());
            if (i.getWiki() != null) console.println(i.getWiki());
            if (i.getSource() != null) console.println(i.getSource());
            if (i.getDonation() != null) console.println(i.getDonation());
            console.println(Arrays.toString(i.getScreenshots()));
            
            console.println(i.getType());
            
            console.println(i.getImplementationType());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

Now let's see all methods of ```IntegrationFile``` interface.
Every integration contains method to get collection of files, so we'll continue the example with this method.

```java
import java.io.PrintStream;
import java.util.Arrays;
import java.io.IOException;

import net.ovonsame.modding.Modding;
import net.ovonsame.modding.interfaces.Integration;
import net.ovonsame.modding.enumeration.Platform;

public class Overview {
    public static void main(String[] args) {
        try {
            Integration i = Modding.getIntegration(Platform.CURSEFORGE, "238222", "key");
            PrintStream console = System.out;
            
            i.getFiles().forEach(f -> {
                Integration integ = f.getParentIntegration();
                
                console.println(integ.getTitle());
                
                console.println(f.getFileName());
                console.println(f.getId());
                console.println(f.getChangelog());
                
                console.println(f.getUrl());
                
                console.println(f.getSize());
                
                console.println(f.getPublished());
                
                console.println(f.getFileDownloads());
                
                console.println(f.getType());
                
                console.println(Arrays.toString(f.getLoaders()));
                
                console.println(Arrays.toString(f.getVersions()));
                
                console.println(f.getSide());
                
                console.println(f.getDependencies());
                
                console.println(f.getPossibleLoaders());
                
                console.println(f.getPossibleVersions());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

Every file has versions which it is available for. To implement versions ```Version``` record class was made. It requires loader and version in constructor.

```java
import net.ovonsame.modding.Version;
import net.ovonsame.modding.enumeration.loader.ModLoader; 

public class Overview {
    public static void main(String[] args) {
        Version version = new Version(ModLoader.FORGE, "1.12.2");
        
        System.out.println(version.isSnapshot());
    } 
}
```

File has collections of dependencies which are presented by ```IntegrationFile``` interface. Collection can be empty if integration has no dependencies.

More than that, the interface has methods to get loaders and versions which are presented by the platform. It's not recommended to use them, because it's not always accurate and can be empty, but integration file can not be without versions. It's better to use ```getPossibleLoaders()``` and ```getPossibleVersions()``` methods.

Also file has environment side which is represented by ```Side``` enumeration. Side represents environment where integration file is for. It can be ```CLIENT```, ```SERVER``` or ```ANY```.

Project has three types of authorities represented as interfaces:
 - ```Author``` a representation of a person registered on a platform
 - ```Team``` a named group of authors, and it's also an ```Iterable``` of ```Author```
 - ```Organisation``` is a subclass of ```Team``` and represents a team which has an owner

Method ```getAuthor``` from class ```Authority``` allows you to get any author from any platform.

```java
import java.io.IOException;
import net.ovonsame.modding.Authority;
import net.ovonsame.modding.interfaces.authority.Author;
import net.ovonsame.modding.enumeration.Platform;

public class Overview {
    public static void main(String[] args) {
        try{
            Author author = Authority.getAuthor(Platform.CURSEFORGE, "name", "key");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
```

In ```Integration``` interface you can always get authors and convert them to a team or an organisation. They would have a typical name if group doesn't have a name.

Below there are all methods of the ```Author``` interfaces.

```java
import java.io.IOException;
import net.ovonsame.modding.Authority;
import net.ovonsame.modding.interfaces.authority.Author;
import net.ovonsame.modding.enumeration.Platform;
import java.io.PrintStream;

public class Overview {
    public static void main(String[] args) {
        try{
            Author author = Authority.getAuthor(Platform.CURSEFORGE, "name", "key");
            PrintStream console = System.out;
            
            console.println(author.getName());
            
            console.println(author.getId());
            
            console.println(author.getRegistered());
            
            console.println(author.getPlatform());
            
            if(author.getAvatar() != null) console.println(author.getAvatar());
            
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
```

### Generating Definitions

Interface ```Integration``` has a default method called "generate". You can use it to generate ```.yaml``` file where is shown how to add every version of the integration to Gradle dependencies. All versions except snapshots will be shown. If your integration is not a mod, plugin or addon, file won't be generated.
It's also can be used to automatically generate APIs for MCreator plugins, because definition file has the same structure as the MCreator's API.

For example there's how to generate JEI definition file in a directory and an output.

```java
import net.ovonsame.modding.Modding;
import net.ovonsame.modding.enumeration.Platform;
import net.ovonsame.modding.interfaces.Integration;
import java.io.IOException;

import java.nio.file.Path;

public class Generation {
    public static void main(String[] args) {
        try {
            Integration i = Modding.getIntegration(Platform.CURSEFORGE, "238222", "key");
            i.generate(new File("./definitions"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

```yaml
---
neoforge-1.21.9:
  gradle: |
    var final gradlev = gradle.gradleVersion.replaceAll(/[^\d.]/, '').toBigDecimal()
    if (gradlev >= 6.2) {
        repositories {
            exclusiveContent {
                forRepository {
                    maven {
                        url "https://cursemaven.com"
                    }
                }

                filter {
                    includeGroup "curse.maven"
                }
            }
        }
    } else if (gradlev >= 5) {
        repositories {
            maven {
                url "https://cursemaven.com"
                content {
                    includeGroup "curse.maven"
                }
            }
        }
    } else {
        repositories {
            maven {
                url "https://cursemaven.com"
            }
        }
    }

    dependencies {
        implementation fg.deobf('curse.maven:jei-238222:7182174')
    }

  update_files:
    - ~

# continue for other versions

name: "Just Enough Items"
```