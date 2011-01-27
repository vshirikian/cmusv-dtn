/*
 *	  This file is part of the Master Thesis "Security in Bytewalla" by Sebastian Domancich.
 *    More information can be found at "http://www.tslab.ssvl.kth.se/csd/projects/1011248/".
 *    
 *    Copyright 2010 Telecommunication Systems Laboratory (TSLab), Royal Institute of Technology, Sweden.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 */

package se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.security;

/**
 * Nested class used to provide storage for key bytes.
 * 
 * @author Sebastian Domancich (sdo@kth.se)
 */

public class Entry 
{
	/**
	 * Constructor.
	 *
	 * @param host     Name of host for whom this key should be used.
	 * @param cs_num   Ciphersuite number this key is to be used with.
	 * @param key      Key data.
	 * @param key_len  Length of key data (in bytes).
	 */
	public Entry(final String host, int cs_num, final String key,
			int key_len)
	{

	}

	/**
	 * Default constructor.
	 */
	public Entry()
	{
	}

	/**
	 * Copy constructor.
	 */
	Entry(Entry other)
	{
	}


	/**
	 * Assignment operator.
	 */
	void operator(Entry other)
	{
	}

	/**
	 * Determine if two entries have the same host and ciphersuite
	 * number.
	 */
	boolean match(Entry other) 
	{
		return false;
	}

	/**
	 * Determine if this entry matches the given host and ciphersuite
	 * number.
	 */
	boolean match(String host, int cs_num)
	{
		return false;
	}

	/**
	 * Same as match(), but also matches wildcard entries (where host
	 * is "*").
	 */
	boolean match_wildcard(String host, int cs_num)
	{
		return false;
	}

	/// @{ Non-mutating accessors.

	String host()
	{
		return host_ ;
	}

	int cs_num()
	{ return cs_num_ ;
	}

	String key()
	{
		return key_;
	}

	int key_len()
	{
		return key_len_ ;
	}
	/// @}

	/**
	 * Dump this entry to a StringBuffer in human-readable form.
	 */
	public void dump(StringBuffer buf) 
	{

	}

	/**
	 * Dump a human-readable header for the output of dump().
	 */
	static void dump_header(StringBuffer buf)
	{
	}

	private String  host_;
	private int   cs_num_;
	private String  key_;
	private int     key_len_;
}