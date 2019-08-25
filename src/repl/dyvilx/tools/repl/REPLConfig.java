package dyvilx.tools.repl;

import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.config.CompilerConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class REPLConfig extends CompilerConfig
{
	// =============== Fields ===============

	private File dumpDir;

	private final List<File> autoLoadFiles = new ArrayList<>();

	// =============== Constructors ===============

	public REPLConfig(DyvilCompiler compiler)
	{
		super(compiler);
	}

	// =============== Properties ===============

	public File getDumpDir()
	{
		return this.dumpDir;
	}

	public void setDumpDir(File dumpDir)
	{
		this.dumpDir = dumpDir;
	}

	public List<File> getAutoLoadFiles()
	{
		return this.autoLoadFiles;
	}

	// =============== Methods ===============

	@Override
	public void addOptions(Options options)
	{
		super.addOptions(options);

		options.addOption(null, "dump-dir", false, "the target directory for bytecode generated by the REPL");
	}

	@Override
	public void readOptions(CommandLine cmd)
	{
		super.readOptions(cmd);

		if (cmd.hasOption("dump-dir"))
		{
			this.setDumpDir(new File(cmd.getOptionValue("dump-dir")));
		}
	}
}
