package dyvil.tools.compiler.ast.structure;

import java.io.File;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.classes.NestedClass;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.classes.DyvilUnitParser;

public final class DyvilUnit extends DyvilHeader
{
	private IClass[]		classes			= new IClass[1];
	private int				classCount;
	private NestedClass[]	innerClasses	= new NestedClass[1];
	private int				innerClassCount;
	
	public DyvilUnit(Package pack, CodeFile input, File output)
	{
		super(pack, input, output);
	}
	
	@Override
	public int classCount()
	{
		return this.classCount;
	}
	
	@Override
	public void addClass(IClass iclass)
	{
		int index = this.classCount++;
		if (index >= this.classes.length)
		{
			IClass[] temp = new IClass[this.classCount];
			System.arraycopy(this.classes, 0, temp, 0, this.classes.length);
			this.classes = temp;
		}
		this.classes[index] = iclass;
	}
	
	@Override
	public IClass getClass(int index)
	{
		return this.classes[index];
	}
	
	@Override
	public IClass getClass(Name name)
	{
		for (int i = 0; i < this.classCount; i++)
		{
			IClass c = this.classes[i];
			if (c.getName() == name)
			{
				return c;
			}
		}
		return null;
	}
	
	@Override
	public int innerClassCount()
	{
		return this.innerClassCount;
	}
	
	@Override
	public void addInnerClass(NestedClass iclass)
	{
		int index = this.innerClassCount++;
		if (index >= this.innerClasses.length)
		{
			NestedClass[] temp = new NestedClass[this.innerClassCount];
			System.arraycopy(this.innerClasses, 0, temp, 0, this.innerClasses.length);
			this.innerClasses = temp;
		}
		this.innerClasses[index] = iclass;
		
		iclass.setIndex(index);
	}
	
	@Override
	public NestedClass getInnerClass(int index)
	{
		return this.innerClasses[index];
	}
	
	@Override
	public void parse()
	{
		ParserManager manager = new ParserManager(new DyvilUnitParser(this));
		manager.semicolonInference = true;
		manager.operators = this.operators;
		manager.parse(this.markers, this.tokens);
		this.tokens = null;
	}
	
	@Override
	public void resolveTypes()
	{
		super.resolveTypes();
		
		for (int i = 0; i < this.classCount; i++)
		{
			IClass iclass = this.classes[i];
			if (iclass.getName() == null)
			{
				this.classes[i] = null;
				this.classCount = i;
				return;
			}
			this.classes[i].resolveTypes(this.markers, this);
		}
	}
	
	@Override
	public void resolve()
	{
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].resolve(this.markers, this);
		}
	}
	
	@Override
	public void checkTypes()
	{
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].checkTypes(this.markers, this);
		}
	}
	
	@Override
	public void check()
	{
		this.pack.check(this.packageDeclaration, this.inputFile, this.markers);
		
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].check(this.markers, this);
		}
	}
	
	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].foldConstants();
		}
	}
	
	@Override
	public void compile()
	{
		int size = this.markers.size();
		if (size > 0)
		{
			StringBuilder buf = new StringBuilder("Problems in Dyvil File ").append(this.inputFile).append(":\n\n");
			String code = this.inputFile.getCode();
			
			int warnings = this.markers.getWarnings();
			int errors = this.markers.getErrors();
			this.markers.sort();
			for (Marker marker : this.markers)
			{
				marker.log(code, buf);
			}
			buf.append(errors).append(errors == 1 ? " Error, " : " Errors, ").append(warnings).append(warnings == 1 ? " Warning" : " Warnings");
			DyvilCompiler.logger.info(buf.toString());
			if (errors > 0)
			{
				DyvilCompiler.logger.warning(this.name + " was not compiled due to errors in the Compilation Unit\n");
				return;
			}
		}
		
		for (int i = 0; i < this.classCount; i++)
		{
			IClass iclass = this.classes[i];
			String name = iclass.getName().qualified;
			if (!name.equals(this.name))
			{
				name = this.name + "$" + name + ".class";
			}
			else
			{
				name += ".class";
			}
			File file = new File(this.outputDirectory, name);
			ClassWriter.saveClass(file, iclass);
			
			IClassBody body = iclass.getBody();
			if (body != null)
			{
				int len = body.classCount();
				for (int j = 0; j < len; j++)
				{
					IClass iclass1 = body.getClass(j);
					name = this.name + "$" + iclass1.getName().qualified + ".class";
					file = new File(this.outputDirectory, name);
					ClassWriter.saveClass(file, iclass1);
				}
			}
		}
		
		for (int i = 0; i < this.innerClassCount; i++)
		{
			NestedClass iclass = this.innerClasses[i];
			String name = iclass.getName().qualified + ".class";
			File file = new File(this.outputDirectory, name);
			ClassWriter.saveClass(file, iclass);
		}
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		// Own classes
		for (int i = 0; i < this.classCount; i++)
		{
			IClass c = this.classes[i];
			if (c.getName() == name)
			{
				return c;
			}
		}
		
		return super.resolveClass(name);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].toString(prefix, buffer);
			if (Formatting.Class.newLine)
			{
				buffer.append('\n');
			}
		}
	}
}