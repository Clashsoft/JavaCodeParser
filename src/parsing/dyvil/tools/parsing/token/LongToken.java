package dyvil.tools.parsing.token;

import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.position.CodePosition;
import dyvil.tools.parsing.position.ICodePosition;

public final class LongToken implements IToken
{
	private IToken	prev;
	private IToken	next;
	
	private final int	lineNumber;
	private final int	start;
	private final int	end;
	
	private long value;
	
	public LongToken(IToken prev, int lineNumber, int start, int end)
	{
		this.prev = prev;
		prev.setNext(this);
		
		this.lineNumber = lineNumber;
		this.start = start;
		this.end = end;
	}
	
	public LongToken(IToken prev, long value, int lineNumber, int start, int end)
	{
		this.prev = prev;
		prev.setNext(this);
		this.value = value;
		
		this.lineNumber = lineNumber;
		this.start = start;
		this.end = end;
	}
	
	public LongToken(long value, int lineNumber, int start, int end)
	{
		this.value = value;
		
		this.lineNumber = lineNumber;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public int type()
	{
		return Tokens.LONG;
	}
	
	@Override
	public long longValue()
	{
		return this.value;
	}
	
	@Override
	public int startIndex()
	{
		return this.start;
	}
	
	@Override
	public int endIndex()
	{
		return this.end;
	}
	
	@Override
	public int startLine()
	{
		return this.lineNumber;
	}
	
	@Override
	public int endLine()
	{
		return this.lineNumber;
	}
	
	@Override
	public void setPrev(IToken prev)
	{
		this.prev = prev;
	}
	
	@Override
	public void setNext(IToken next)
	{
		this.next = next;
	}
	
	@Override
	public IToken prev()
	{
		return this.prev;
	}
	
	@Override
	public IToken next()
	{
		return this.next;
	}
	
	@Override
	public boolean hasNext()
	{
		return this.next != null;
	}
	
	@Override
	public boolean hasPrev()
	{
		return this.prev != null;
	}
	
	@Override
	public void setLong(long value)
	{
		this.value = value;
	}
	
	@Override
	public ICodePosition raw()
	{
		return new CodePosition(this.lineNumber, this.start, this.endIndex());
	}
	
	@Override
	public ICodePosition to(ICodePosition end)
	{
		return new CodePosition(this.lineNumber, end.endLine(), this.start, end.endIndex());
	}
	
	@Override
	public String toString()
	{
		return "Long " + this.value;
	}
}
