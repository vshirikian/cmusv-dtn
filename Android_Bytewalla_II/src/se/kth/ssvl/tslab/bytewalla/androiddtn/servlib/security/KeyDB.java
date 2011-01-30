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

import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.List;


/**
 * 
 * 
 * @author Sebastian Domancich (sdo@kth.se)
 */
public class KeyDB 
{

	/**
	 * Part of the Singleton implementation 
	 */
	private static KeyDB instance_ = null;

	/**
	 * Singleton implementation of DTNScheme
	 * @return the instance of DTNScheme
	 */
	public static KeyDB getInstance() 
	{
		if (instance_ == null) {
			instance_ = new KeyDB();
		}
		return instance_;
	}
	// End Singleton Implementation of the DTNScheme

	/**
	 * Constructor (called at startup).
	 * @return 
	 */
	public KeyDB()
	{
	}

	/**
	 * Startup-time initializer.
	 */
	public static void init()
	{

	}

	/**
	 * Set the key for a given host and ciphersuite type.  This will
	 * overwrite any existing entry for the same host/ciphersuite.
	 *
	 * XXX Eventually we'd probably want to index by KeyID too, which
	 * is how we'd distinguish between multiple keys for the same
	 * host.
	 */
	static void set_key(Entry entry)
	{
	}

	/**
	 * Find the key for a given host and ciphersuite type.
	 *
	 * @return A pointer to the matching Entry, or NULL if no
	 * match is found.
	 */
	static final Entry find_key(String host, int cs_num)
	{
		return null;
	}

	/**
	 * Delete the key entry for the given host and ciphersuite type.
	 */
	static void del_key(String host, int cs_num)
	{
	}

	/**
	 * Delete all key entries (including wildcard entries).
	 */
	static void flush_keys()
	{

	}

	/**
	 * Dump the contents of the KeyDB in human-readable form.
	 */
	static void dump(StringBuffer buf)
	{

	}

	/**
	 * Dump a human-readable header for the output of dump().
	 */
	static void dump_header(StringBuffer buf)
	{

	}

	/**
	 * Validate the specified ciphersuite number (see if it
	 * corresponds to a registered ciphersuite).
	 */
	static boolean validate_cs_num(int cs_num)
	{
		return false;
	}

	/**
	 * Validate that the specified key length matches what's expected
	 * for the specified ciphersuite.  Stores the expected length back
	 * into the key_len argument if it's wrong.
	 */
	static boolean validate_key_len(int cs_num, int key_len)  //por variable!!!
	{
		return false;
	}

	private List<Entry> keys_;
	
};