# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.6.7/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.6.7/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.6.7/reference/htmlsingle/#boot-features-developing-web-applications)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)

## create CommandServer
```java
package emu.grasscutter.netty;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandMap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

import java.text.MessageFormat;
import java.util.Objects;

import static emu.grasscutter.utils.Language.translate;

public class CommandServer {
    private static ChannelFuture cf;
    private static Channel channel;
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;
    private static final int inetPort = 8824;

    public static void init() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(1);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                socketChannel.pipeline().addLast(new StringDecoder());
                                socketChannel.pipeline().addLast(new ChannelHandle());
                            }
                        });
        try {
            cf = serverBootstrap.bind(inetPort).sync();
            Grasscutter.getLogger().info("Start Command Server on port: {} Success", inetPort);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void close() {
        try {
            channel = cf.channel();
            channel.closeFuture().sync();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public static void messageBack(String msg) {
        if (Objects.nonNull(channel)) {
            String commandFormat = MessageFormat.format("SERVER_COMMAND_MESSAGE::{0}", msg);
            channel.writeAndFlush(Unpooled.wrappedBuffer(commandFormat.getBytes()));
        }
    }

    static class ChannelHandle extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            super.channelRead(ctx, msg);
            channel = ctx.channel();
            String commandStr = msg.toString();
            if (commandStr.startsWith("SERVER_COMMAND::")) {
                String input = commandStr.replace("SERVER_COMMAND::", "");
                Grasscutter.getLogger().info("get command: " + input);
                try {
                    CommandMap.getInstance().invoke(null, null, input);
                } catch (Exception e) {
                    Grasscutter.getLogger().error(translate("messages.game.command_error"), e);
                }
            }
        }
    }

}


```

## edit CommandHandler
```java
package emu.grasscutter.command;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.netty.CommandServer;

import java.util.List;

public interface CommandHandler {
    /**
     * Send a message to the target.
     *
     * @param player  The player to send the message to, or null for the server console.
     * @param message The message to send.
     */
    static void sendMessage(Player player, String message) {
        if (player == null) {
            Grasscutter.getLogger().info(message);
            CommandServer.messageBack(message);
        } else {
            player.dropMessage(message);
        }
    }

    /**
     * Called when a player/console invokes a command.
     * @param sender The player/console that invoked the command.
     * @param args The arguments to the command.
     */
    default void execute(Player sender, Player targetPlayer, List<String> args) {
    }
}

```

## edit config
```java
package emu.grasscutter;

import java.util.Locale;
import emu.grasscutter.Grasscutter.ServerDebugMode;
import emu.grasscutter.Grasscutter.ServerRunMode;
import emu.grasscutter.game.mail.Mail;

public final class Config {
	public String DatabaseUrl = "mongodb://grasscutter:wzp15179688869@43.138.56.77:27017";
	public String DatabaseCollection = "grasscutter";

	public String RESOURCE_FOLDER = "./resources/";
	public String DATA_FOLDER = "./data/";
	public String PACKETS_FOLDER = "./packets/";
	public String DUMPS_FOLDER = "./dumps/";
	public String KEY_FOLDER = "./keys/";
	public String SCRIPTS_FOLDER = "./resources/Scripts/";
	public String PLUGINS_FOLDER = "./plugins/";
	public String LANGUAGE_FOLDER = "./languages/";

	public ServerDebugMode DebugMode = ServerDebugMode.NONE; // ALL, MISSING, NONE
	public ServerRunMode RunMode = ServerRunMode.HYBRID; // HYBRID, DISPATCH_ONLY, GAME_ONLY
	public GameServerOptions GameServer = new GameServerOptions();
	public DispatchServerOptions DispatchServer = new DispatchServerOptions();
	public Locale LocaleLanguage = Locale.CHINESE;
	public Locale DefaultLanguage = Locale.CHINESE;

	public Boolean OpenStamina = true;
	public GameServerOptions getGameServerOptions() {
		return GameServer;
	}

	public DispatchServerOptions getDispatchOptions() { return DispatchServer; }

	public static class DispatchServerOptions {
		public String Ip = "0.0.0.0";
		public String PublicIp = "43.138.56.77";
		public int Port = 443;
		public int PublicPort = 0;
//		public String KeystorePath = "./keystore.p12";
		public String KeystorePath = "./test.keystore";
		public String KeystorePassword = "123456";
		public Boolean UseSSL = true;
		public Boolean FrontHTTPS = true;
		public Boolean CORS = false;
		public String[] CORSAllowedOrigins = new String[] { "*" };

		public boolean AutomaticallyCreateAccounts = false;
		public String[] defaultPermissions = new String[] { "" };

		public RegionInfo[] GameServers = {};

		public RegionInfo[] getGameServers() {
			return GameServers;
		}

		public static class RegionInfo {
			public String Name = "os_usa";
			public String Title = "Test";
			public String Ip = "127.0.0.1";
			public int Port = 22102;
		}
	}

	public static class GameServerOptions {
		public String Name = "Test";
		public String Ip = "0.0.0.0";
		public String PublicIp = "43.138.56.77";
		public int Port = 22102;
		public int PublicPort = 0;

		public String DispatchServerDatabaseUrl = "mongodb://grasscutter:wzp15179688869@43.138.56.77:27017";
		public String DispatchServerDatabaseCollection = "grasscutter";

		public int InventoryLimitWeapon = 2000;
		public int InventoryLimitRelic = 2000;
		public int InventoryLimitMaterial = 2000;
		public int InventoryLimitFurniture = 2000;
		public int InventoryLimitAll = 30000;
		public int MaxAvatarsInTeam = 4;
		public int MaxAvatarsInTeamMultiplayer = 4;
		public int MaxEntityLimit = 1000; // Max entity limit per world. // TODO: Enforce later.
		public boolean WatchGacha = false;
		public String ServerNickname = "Server";
		public int ServerAvatarId = 10000007;
		public int ServerNameCardId = 210001;
		public int ServerLevel = 1;
		public int ServerWorldLevel = 1;
		public String ServerSignature = "Server Signature";
		public int[] WelcomeEmotes = {2007, 1002, 4010};
		public String WelcomeMotd = "Welcome to Grasscutter emu";
		public String WelcomeMailTitle = "Welcome to Grasscutter!";
		public String WelcomeMailSender = "Lawnmower";
		public String WelcomeMailContent = "Hi there!\r\nFirst of all, welcome to Grasscutter. If you have any issues, please let us know so that Lawnmower can help you! \r\n\r\nCheck out our:\r\n<type=\"browser\" text=\"Discord\" href=\"https://discord.gg/T5vZU6UyeG\"/>";
		public Mail.MailItem[] WelcomeMailItems = {
				new Mail.MailItem(13509, 1, 1),
				new Mail.MailItem(201, 10000, 1),
		};

		public boolean EnableOfficialShop = true;

		public GameRates Game = new GameRates();

		public GameRates getGameRates() { return Game; }

		public static class GameRates {
			public float ADVENTURE_EXP_RATE = 1.0f;
			public float MORA_RATE = 1.0f;
			public float DOMAIN_DROP_RATE = 1.0f;
		}
	}
}

```

## edit Grasscutter
```java
package emu.grasscutter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOError;
import java.util.Calendar;

import emu.grasscutter.command.CommandMap;
import emu.grasscutter.netty.CommandServer;
import emu.grasscutter.plugin.PluginManager;
import emu.grasscutter.plugin.api.ServerHook;
import emu.grasscutter.scripts.ScriptLoader;
import emu.grasscutter.utils.Utils;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.qos.logback.classic.Logger;
import emu.grasscutter.data.ResourceLoader;
import emu.grasscutter.database.DatabaseManager;
import emu.grasscutter.utils.Language;
import emu.grasscutter.server.dispatch.DispatchServer;
import emu.grasscutter.server.game.GameServer;
import emu.grasscutter.tools.Tools;
import emu.grasscutter.utils.Crypto;

import static emu.grasscutter.utils.Language.translate;

public final class Grasscutter {
	private static final Logger log = (Logger) LoggerFactory.getLogger(Grasscutter.class);
	private static LineReader consoleLineReader = null;

	private static Config config;
	private static Language language;

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static final File configFile = new File("./config.json");

	private static int day; // Current day of week.

	private static DispatchServer dispatchServer;
	private static GameServer gameServer;
	private static PluginManager pluginManager;

	public static final Reflections reflector = new Reflections("emu.grasscutter");
  
	static {
		// Declare logback configuration.
		System.setProperty("logback.configurationFile", "src/main/resources/logback.xml");

		// Load server configuration.
		Grasscutter.loadConfig();

		// Load translation files.
		Grasscutter.loadLanguage();

		// Check server structure.
		Utils.startupCheck();
	}

  public static void main(String[] args) throws Exception {
    	Crypto.loadKeys(); // Load keys from buffers.

		// Parse arguments.
		boolean exitEarly = false;
		for (String arg : args) {
			switch (arg.toLowerCase()) {
				case "-handbook" -> {
					Tools.createGmHandbook(); exitEarly = true;
				}
				case "-gachamap" -> {
					Tools.createGachaMapping(Grasscutter.getConfig().DATA_FOLDER + "/gacha_mappings.js"); exitEarly = true;
				}
			}
		} 
		
		// Exit early if argument sets it.
		if(exitEarly) System.exit(0);

		// Initialize server.
		Grasscutter.getLogger().info(translate("messages.status.starting"));

		// Load all resources.
		Grasscutter.updateDayOfWeek();
		ResourceLoader.loadAll();
		ScriptLoader.init();

		// Initialize database.
		DatabaseManager.initialize();

		// Create server instances.
		dispatchServer = new DispatchServer();
		gameServer = new GameServer();
		// Create a server hook instance with both servers.
		new ServerHook(gameServer, dispatchServer);
		// Create plugin manager instance.
		pluginManager = new PluginManager();

		// Start servers.
		if (getConfig().RunMode == ServerRunMode.HYBRID) {
			dispatchServer.start();
			gameServer.start();
		} else if (getConfig().RunMode == ServerRunMode.DISPATCH_ONLY) {
			dispatchServer.start();
		} else if (getConfig().RunMode == ServerRunMode.GAME_ONLY) {
			gameServer.start();
		} else {
			getLogger().error(translate("messages.status.run_mode_error", getConfig().RunMode));
			getLogger().error(translate("messages.status.run_mode_help"));
			getLogger().error(translate("messages.status.shutdown"));
			System.exit(1);
		}

		// Enable all plugins.
		pluginManager.enablePlugins();

		new Thread(CommandServer::init).start();

		// Hook into shutdown event.
		Runtime.getRuntime().addShutdownHook(new Thread(Grasscutter::onShutdown));



		// Open console.
		// startConsole();
 }

	/**
	 * Server shutdown event.
	 */
	private static void onShutdown() {
		// Disable all plugins.
		pluginManager.disablePlugins();
	}

	public static void loadConfig() {
		try (FileReader file = new FileReader(configFile)) {
			config = gson.fromJson(file, Config.class);
			saveConfig();
		} catch (Exception e) {
			Grasscutter.config = new Config(); 
			saveConfig();
		}
	}

	public static void loadLanguage() {
		var locale = config.LocaleLanguage;
		var languageTag = locale.toLanguageTag();
		
		if (languageTag.equals("und")) {
			Grasscutter.getLogger().error("Illegal locale language, using 'en-US' instead.");
			language = Language.getLanguage("en-US");
		} else {
			language = Language.getLanguage(languageTag);
		}
	}

	public static void saveConfig() {
		try (FileWriter file = new FileWriter(configFile)) {
			file.write(gson.toJson(config));
		} catch (Exception e) {
			Grasscutter.getLogger().error("Unable to save config file.");
		}
	}

	public static void startConsole() {
		// Console should not start in dispatch only mode.
		if (getConfig().RunMode == ServerRunMode.DISPATCH_ONLY) {
			getLogger().info(translate("messages.dispatch.no_commands_error"));
			return;
		}

		getLogger().info(translate("messages.status.done"));
		String input = null;
		boolean isLastInterrupted = false;
		while (true) {
			try {
				input = consoleLineReader.readLine("> ");
			} catch (UserInterruptException e) {
				if (!isLastInterrupted) {
					isLastInterrupted = true;
					Grasscutter.getLogger().info("Press Ctrl-C again to shutdown.");
					continue;
				} else {
					Runtime.getRuntime().exit(0);
				}
			} catch (EndOfFileException e) {
				Grasscutter.getLogger().info("EOF detected.");
				continue;
			} catch (IOError e) {
				Grasscutter.getLogger().error("An IO error occurred.", e);
				continue;
			}

			isLastInterrupted = false;
			try {
				CommandMap.getInstance().invoke(null, null, input);
			} catch (Exception e) {
				Grasscutter.getLogger().error(translate("messages.game.command_error"), e);
			}
		}
	}

	public static Config getConfig() {
		return config;
	}

	public static Language getLanguage() {
		return language;
	}

	public static Logger getLogger() {
		return log;
	}

	public static LineReader getConsole() {
		if (consoleLineReader == null) {
			Terminal terminal = null;
			try {
				terminal = TerminalBuilder.builder().jna(true).build();
			} catch (Exception e) {
				try {
					// Fallback to a dumb jline terminal.
					terminal = TerminalBuilder.builder().dumb(true).build();
				} catch (Exception ignored) {
					// When dumb is true, build() never throws.
				}
			}
			consoleLineReader = LineReaderBuilder.builder()
					.terminal(terminal)
					.build();
		}
		return consoleLineReader;
	}

	public static Gson getGsonFactory() {
		return gson;
	}

	public static DispatchServer getDispatchServer() {
		return dispatchServer;
	}

	public static GameServer getGameServer() {
		return gameServer;
	}

	public static PluginManager getPluginManager() {
		return pluginManager;
	}

	public static void updateDayOfWeek() {
		Calendar calendar = Calendar.getInstance();
		day = calendar.get(Calendar.DAY_OF_WEEK); 
	}

	public static int getCurrentDayOfWeek() {
		return day;
	}

	public enum ServerRunMode {
		HYBRID, DISPATCH_ONLY, GAME_ONLY
	}

	public enum ServerDebugMode {
		ALL, MISSING, NONE
	}
}

```

```
43.138.56.77

mongodb://grasscutter:wzp15179688869@43.138.56.77:27017

http://43.138.56.77:8000/command
```

## mitmproxy 代理服务器

[原神 2.6 私服启动教程2.2](https://blog.otoo.top/Blog/Genshin2-6-Grasscutters/)
