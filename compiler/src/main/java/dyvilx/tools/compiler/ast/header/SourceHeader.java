package dyvilx.tools.compiler.ast.header;

import dyvil.reflect.Modifiers;
import dyvil.source.FileSource;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.backend.ObjectFormat;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.lang.I18n;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.SemicolonInference;
import dyvilx.tools.compiler.parser.header.SourceFileParser;
import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.ParserManager;
import dyvilx.tools.parsing.TokenList;
import dyvilx.tools.parsing.lexer.DyvilLexer;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

public class SourceHeader extends AbstractHeader implements ISourceHeader, IDefaultContext
{
	// =============== Fields ===============

	protected TokenList  tokens;
	protected MarkerList markers = new MarkerList(Markers.INSTANCE);

	public final FileSource fileSource;
	public final File       outputDirectory;

	protected final DyvilCompiler compiler;

	// =============== Constructors ===============

	public SourceHeader(DyvilCompiler compiler, Package pack, File input, File output)
	{
		this.compiler = compiler;
		this.pack = pack;

		this.fileSource = new FileSource(input);
		this.name = Util.getHeaderName(input);
		this.outputDirectory = output.getParentFile();
	}

	// =============== Properties ===============

	@Override
	public MarkerList getMarkers()
	{
		return this.markers;
	}

	@Override
	public DyvilCompiler getCompilationContext()
	{
		return this.compiler;
	}

	@Override
	public FileSource getFileSource()
	{
		return this.fileSource;
	}

	@Override
	public HeaderDeclaration getHeaderDeclaration()
	{
		if (this.headerDeclaration == null && this.needsDefaultHeaderDeclaration())
		{
			final int modifiers = this.getDefaultHeaderDeclarationVisibility() | Modifiers.GENERATED;
			final AttributeList attributes = AttributeList.of(modifiers);

			return this.headerDeclaration = new HeaderDeclaration(this, SourcePosition.ORIGIN, this.name, attributes);
		}
		return this.headerDeclaration;
	}

	protected boolean needsDefaultHeaderDeclaration()
	{
		return true;
	}

	protected int getDefaultHeaderDeclarationVisibility()
	{
		return Modifiers.PUBLIC;
	}

	// =============== Methods ===============

	// --------------- Phases ---------------

	protected boolean load()
	{
		try
		{
			this.fileSource.load();
			return true;
		}
		catch (IOException ex)
		{
			this.compiler.error(I18n.get("source.error", this.fileSource.file()), ex);
			return false;
		}
	}

	@Override
	public void tokenize()
	{
		if (this.load())
		{
			this.tokens = new DyvilLexer(this.markers, DyvilSymbols.INSTANCE).tokenize(this.fileSource.text());
			SemicolonInference.inferSemicolons(this.tokens.first());
		}
	}

	@Override
	public void parse()
	{
		new ParserManager(DyvilSymbols.INSTANCE, this.tokens.iterator(), this.markers)
			.parse(new SourceFileParser(this).withFlags(SourceFileParser.NO_CLASSES));
	}

	@Override
	public void resolveHeaders()
	{
		for (int i = 0; i < this.importCount; i++)
		{
			this.importDeclarations[i].resolveTypes(this.markers, this);
		}
	}

	@Override
	public void resolveTypes()
	{
		final IContext context = this.getContext();

		for (int i = 0; i < this.typeAliasCount; i++)
		{
			this.typeAliases[i].resolveTypes(this.markers, context);
		}

		this.classes.resolveTypes(this.markers, context);
	}

	@Override
	public void resolve()
	{
		for (int i = 0; i < this.importCount; i++)
		{
			this.importDeclarations[i].resolve(this.markers, this);
		}

		this.classes.resolve(this.markers, this.getContext());
	}

	@Override
	public void checkTypes()
	{
		this.classes.checkTypes(this.markers, this.getContext());
	}

	@Override
	public void check()
	{
		PackageDeclaration.check(this.packageDeclaration, this.markers, this.pack);

		final HeaderDeclaration headerDeclaration = this.getHeaderDeclaration();
		if (headerDeclaration != null)
		{
			headerDeclaration.check(this.markers);
		}

		this.classes.check(this.markers, this.getContext());
	}

	@Override
	public void foldConstants()
	{
		this.classes.foldConstants();
	}

	@Override
	public void cleanup()
	{
		this.classes.cleanup(this, null);
	}

	protected boolean printMarkers()
	{
		return ICompilationUnit
			       .printMarkers(this.compiler, this.markers, DyvilFileType.DYVIL_HEADER, this.name, this.fileSource);
	}

	@Override
	public void compile()
	{
		if (this.printMarkers())
		{
			return;
		}

		if (this.getHeaderDeclaration() != null)
		{
			final File file = new File(this.outputDirectory, this.name.qualified + DyvilFileType.OBJECT_EXTENSION);
			ObjectFormat.write(this.compiler, file, this);
		}

		for (IClass iclass : this.classes)
		{
			ClassWriter.compile(this.compiler, new File(this.outputDirectory, iclass.getFileName()), iclass);
		}

		for (int i = 0; i < this.innerClassCount; i++)
		{
			final ICompilable compilable = this.innerClasses[i];
			final File file = new File(this.outputDirectory, compilable.getFileName());
			ClassWriter.compile(this.compiler, file, compilable);
		}
	}

	// --------------- Compilation ---------------

	@Override
	public void read(DataInput in) throws IOException
	{
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		// Header Name
		this.headerDeclaration.write(out);

		// Import Declarations
		int imports = this.importCount;
		out.writeShort(imports);
		for (int i = 0; i < imports; i++)
		{
			this.importDeclarations[i].write(out);
		}

		// Operators Definitions
		out.writeShort(this.operatorCount);
		for (int i = 0; i < this.operatorCount; i++)
		{
			this.operators[i].writeData(out);
		}

		// Type Aliases
		out.writeShort(this.typeAliasCount);
		for (int i = 0; i < this.typeAliasCount; i++)
		{
			this.typeAliases[i].write(out);
		}

		// Classes
		out.writeShort(this.classes.size());
		for (IClass iclass : this.classes)
		{
			out.writeUTF(iclass.getInternalName());
		}
	}
}
