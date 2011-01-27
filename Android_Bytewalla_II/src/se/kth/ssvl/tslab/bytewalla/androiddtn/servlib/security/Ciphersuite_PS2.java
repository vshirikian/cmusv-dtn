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

import java.nio.ByteBuffer;

import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockInfo;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockInfoVec;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.Bundle;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockProcessor.OpaqueContext;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleProtocol.status_report_reason_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.contacts.Link;

/**
 * "Block processor implementation for the bundle authentication block."[DTN2]
 * 
 * @author Sebastian Domancich (sdo@kth.se)
 */
public class Ciphersuite_PS2 extends Ciphersuite 
{
	//Constructor
	public Ciphersuite_PS2()
	{

	}

	public int cs_num()
	{
		return 0;
	}

	public int result_len() 
	{ 
		return res_len; 
	}

	/**
	 * "First callback for parsing blocks that is expected to append a
	 * chunk of the given data to the given block. When the block is
	 * completely received, this should also parse the block into any
	 * fields in the bundle class.
	 *
	 * The base class implementation parses the block preamble fields
	 * to find the length of the block and copies the preamble and the
	 * data in the block's contents buffer.
	 *
	 * This and all derived implementations must be able to handle a
	 * block that is received in chunks, including cases where the
	 * preamble is split into multiple chunks."[DTN2]
	 *
	 * @return the amount of data consumed or -1 on error
	 */
	public int consume (Bundle bundle, BlockInfo block, ByteBuffer buf, int len)
	{
		return 0;
	}

	/**
	 * "Validate the block. This is called after all blocks in the
	 * bundle have been fully received."[DTN2]
	 *
	 * @return true if the block passes validation
	 */

	public boolean validate(final Bundle           bundle,
			BlockInfoVec           block_list,
			BlockInfo              block,
			status_report_reason_t reception_reason,
			status_report_reason_t deletion_reason)
	{
		return false;
	}

	/**
	 * First callback to generate blocks for the output pass. The
	 * function is expected to initialize an appropriate BlockInfo
	 * structure in the given BlockInfoVec.
	 *
	 * The base class simply initializes an empty BlockInfo with the
	 * appropriate owner_ pointer.
	 */

	public int prepare(final Bundle    bundle,
			BlockInfoVec    xmit_blocks,
			final BlockInfo source,
			final Link   link,
			BlockInfo.list_owner_t     list)
	{
		return 0;
	}


	/**
	 * Second callback for transmitting a bundle. This pass should
	 * generate any data for the block that does not depend on other
	 * blocks' contents.  It MUST add any EID references it needs by
	 * calling block->add_eid(), then call generate_preamble(), which
	 * will add the EIDs to the primary block's dictionary and write
	 * their offsets to this block's preamble.
	 */

	public int generate (final Bundle  bundle,
			BlockInfoVec  xmit_blocks,
			BlockInfo     block,
			final Link link,
			boolean           last) 
	{
		return 0;
	}

	/**
	 * Third callback for transmitting a bundle. This pass should
	 * generate any data (such as security signatures) for the block
	 * that may depend on other blocks' contents.
	 *
	 * The base class implementation does nothing. 
	 */
	public int finalize(final Bundle  bundle, 
			BlockInfoVec  xmit_blocks, 
			BlockInfo     block, 
			final Link link) 
	{
		return 0;
	}

	void digest(final Bundle    bundle,
			final BlockInfo caller_block,
			final BlockInfo target_block,
			final char      buf,
			int           len,
			OpaqueContext   r)
	{
	}

	/**
	 * Defined size, in bytes, for security result.
	 * For SHA-1 this is 20 bytes (160 bits)
	 */
	final int res_len=20;
	
	final static int CSNUM_PS2 =2;      
}