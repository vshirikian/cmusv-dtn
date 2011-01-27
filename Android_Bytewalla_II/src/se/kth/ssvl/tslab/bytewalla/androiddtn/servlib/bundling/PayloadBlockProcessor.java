/*
 *	  This file is part of the Bytewalla Project
 *    More information can be found at "http://www.tslab.ssvl.kth.se/csd/projects/092106/".
 *    
 *    Copyright 2009 Telecommunication Systems Laboratory (TSLab), Royal Institute of Technology, Sweden.
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

package se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling;

import java.io.Serializable;

import android.util.Log;

import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockProcessor.OpaqueContext;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockProcessor.mutate_func;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockProcessor.mutate_func_event_data;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundlePayload.location_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleProtocol.block_flag_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleProtocol.bundle_block_type_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleProtocol.status_report_reason_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.contacts.Link;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.BufferHelper;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.IByteBuffer;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.SerializableByteBuffer;


/**
 * This class extends BlockProcessor and is the implementation of the payload bundle block.
 * 
 * @author Sharjeel Ahmed (sharjeel@kth.se) 
 */

public class PayloadBlockProcessor extends BlockProcessor implements Serializable{

	
	/**
	 * SerialVersionID to Support Serializable.
	 */

	private static final long serialVersionUID = 6716094908846439288L;
	
	
	/**
	 * TAG for Android Logging
	 */
	public static String TAG = "PayloadBlockProcessor";
	
	/**
	 * Constructor 
	 */
    public PayloadBlockProcessor(){
    	super(bundle_block_type_t.PAYLOAD_BLOCK);
    }

    
	/**
	 * "Similar to process() but for potentially mutating processing functions.
	 * The function returns true iff it modified the target_block." [DTN2]
	 * 
	 * Function used for security purposes. When a block is going to be encrypted, ciphersuiteC3.finalize calls 
	 * this function with the pointer to do_crypt, defined inside finalize.
	 * @param func (IN): function to call
	 * @param bundle ():
	 * @param caller_block (IN): security block
	 * @param target_block (OUT): payload block that we are going to change
	 * @param offset (): data offset
	 * @param len (IN): data length
	 * @param r (IN): nu se.
     * @return  Return number of bytes successfully consumed
	 */
	public boolean mutate(mutate_func     func,
                        Bundle          bundle,
                        final BlockInfo caller_block,
                        BlockInfo       target_block,
                        int           offset,
                        int           len,
                        OpaqueContext   r)
    {
    	
        
        
      /*
        
        // "convert the offset to a pointer in the target block" [DTN2]
        IByteBuffer buf = target_block.contents();
        buf.position( buf.position()+ offset);
        
        
	    */
	    
	    
	    
	    
	    
	    
	    
	    boolean changed = false;
	    IByteBuffer buf;
	    //char  work[1024];         // XXX/pl TODO rework buffer usage
	    //    and look at type for the payload data
	    int  len_to_do = 0;
	    
	    // re-do these appropriately for the payload
	    //ASSERT(offset < target_block->contents().len());
	    //ASSERT(target_block->contents().len() >= offset + len);
	    
	    // First work on specified range of the preamble
	    if (offset < target_block.data_offset()) 
	    {
	        len_to_do = Math.min(len, target_block.data_offset() - offset);
	        // convert the offset to a pointer in the target block
	        
	        buf = target_block.contents();
	        buf.position(buf.position()+offset);
	        // call the processing function to do the work
	        
	     // "call the mutating function to do the work" [DTN2]
	        mutate_func_event_data data = new mutate_func_event_data();
		    data.set_bundle(bundle);
		    data.set_caller_block(caller_block);
		    data.set_target_block(target_block);
		    data.set_buf(buf);
		    data.set_len(len_to_do);
		    data.set_context(r);
		    changed= func.action(data);
		    
		    
		    
	       
	        
	        buf.position(buf.position()+len_to_do);
	        offset += len_to_do;
	        len    -= len_to_do;
	    }

	    if (len == 0)
	        return changed;

	    // Adjust offset to account for the preamble
	    int payload_offset = offset - target_block.data_offset();
	    int remaining = Math.min(len, bundle.payload().length() - payload_offset);
	    int outlen; 

	    //buf = new SerializableByteBuffer(5000*1024);     // use local buffer, max=10k
	    buf = new SerializableByteBuffer(2100000);     // use local buffer, max=10k

	    while (remaining >0) 
	    {       
	    	Log.d(TAG, "mutate(): We start a pass in the while loop to mutate payload"); 
	        outlen = 0; 
	        len_to_do = Math.min(remaining, buf.capacity());   
	        bundle.payload().read_data(payload_offset, len_to_do, buf);
	        
	        // call the processing function to do the work
	        //boolean chunk_changed =(*func)(bundle, caller_block, target_block, buf, len_to_do, r);
	        
	        mutate_func_event_data data = new mutate_func_event_data();
		    data.set_bundle(bundle);
		    data.set_caller_block(caller_block);
		    data.set_target_block(target_block);
		    data.set_buf(buf);
		    data.set_len(len_to_do);
		    data.set_context(r);
		    boolean chunk_changed = func.action(data);
		    
		    
		    
		    
	        
	        // if we need to flush changed content back to disk, do it .
		    //sd. In android, we dont do it yet, because we havent obtained the ciphertext yet!!!
	        //if ( chunk_changed )            
	        //    bundle.payload().write_data(buf, payload_offset, len_to_do);
		    
		    
		    assert(chunk_changed == true):TAG+"mutate(): ERROR. A payload change is not going to happen!";
	        
	        changed |= chunk_changed;
	                   
	        payload_offset  += len_to_do;
	        remaining       -= len_to_do;
	    }

	    return changed;
	}
	
	
	
	
	
    
	
	
	
    /**
     * This function consumes the payload block of the bundle. It is a 
     * virtual from BlockProcessor.
     * @param bundle Bundle to set data after consuming
     * @param blcok Payload block to set data after consuming
     * @param buf Populated buffer to read data from for consuming 
     * @param len Number of bytes to consume
     * @return  Return number of bytes successfully consumed
     */
    public int consume(Bundle    bundle,
                BlockInfo block,
                IByteBuffer    buf,
                int     len){
    	
        BlockInfoVec recv_blocks = bundle.recv_blocks();
        int consumed = 0;
        
        int[] flags = new int[1];
        if (block.data_offset() == 0) {
            int cc = super.consume_preamble(recv_blocks, block, buf, len, flags);
            if (cc == -1) {
                return -1;
            }

            buf.position(buf.position()+cc);
            len -= cc;
            consumed += cc;
            Log.d(TAG, "in len: "+len+" : cc"+cc+" "+ bundle.payload().length());
            assert(bundle.payload().length() == 0)
            :TAG+"consume() bundle payload length is not 0";
        }        
        
        Log.d(TAG, "out len: "+len+" : cc"+consumed+" "+ bundle.payload().length());

        if (block.data_offset() == 0) {
            assert(len == 0)
            :TAG+": consume() len!=0";
            return consumed;
        }

        // Special case for the simulator -- if the payload location is
        // NODATA, then we're done.
        if (bundle.payload().location().getCode() == location_t.NODATA.getCode()) {
            block.set_complete(true);
            return consumed;
        }

        // If we've consumed the length (because the data_offset is
        // non-zero) and the length is zero, then we're done.
        if (block.data_offset() != 0 && block.data_length() == 0) {
            block.set_complete(true);
            return consumed;
        }

        // Also bail if there's nothing left to do
        if (len == 0) {
            return consumed;
        }

        // Otherwise, the buffer should always hold just the preamble
        // since we store the rest in the payload file
        assert(block.contents().capacity() == block.data_offset())
        :TAG+": consume() data_offset not equal to content capacity";
        
        // Now make sure there's still something left to do for the block,
        // otherwise it should have been marked as complete
        assert(block.data_length() > bundle.payload().length());

        int rcvd      = (int)bundle.payload().length();
        int remainder = block.data_length() - rcvd;
        int tocopy;

        if (len >= remainder) {
            block.set_complete(true);
            tocopy = remainder;
        } else {
            tocopy = len;
        }

        bundle.payload().set_length(rcvd + tocopy);
        bundle.payload().write_data(buf, rcvd, tocopy);

        consumed += tocopy;

        Log.d(TAG, String.format("consumed %s/%s (%s)",
                    consumed, block.full_length(), 
                    block.complete() ? "complete" : "not complete"));
        
        return consumed;        
    	
    }
    
    /**
	 * Generate the preamble for the payload block.
	 * @param bundle Bundle to generate preamble
	 * @param xmit_blocks xmit_blocks to get the dictionary for generating preamble
	 * @param block Block to write the payload preamble
	 * @param link Link type
	 * @param last If its a last block
	 * @return If successfully generated then return Success message.  
     */
    public int generate(final Bundle  bundle,
                 BlockInfoVec  xmit_blocks,
                 BlockInfo     block,
                 final Link link,
                 boolean           last){
        // in the ::generate pass, we just need to set up the preamble,
        // since the payload stays on disk
        super.generate_preamble(xmit_blocks, 
                          block,
                          bundle_block_type_t.PAYLOAD_BLOCK,
                          last ? block_flag_t.BLOCK_FLAG_LAST_BLOCK.getCode() : 0,
                          bundle.payload().length());

        return BP_SUCCESS;
    }
    
    /**
     * Check the validity of the Payload block
     * @param bundle Bundle to check the generic validity of block
     * @param block_list List of Blocks
     * @param block Block to check if it's valid or not
     * @param reception_reason If block is not valid then reception reason
     * @param deletion_reason If block is not balid then deletion reason
     * 
     * @return True if the block is valid else false
     */
    public boolean validate(final Bundle           bundle,
                  BlockInfoVec           block_list,
                  BlockInfo              block,
                  status_report_reason_t[] reception_reason,
                  status_report_reason_t[] deletion_reason){
        // check for generic block errors
        if (!super.validate(bundle, block_list, block,
                                      reception_reason, deletion_reason)) {
            return false;
        }

        if (!block.complete()) {
            // We do not need the block to be complete because we may be
            // able to reactively fragment it, but we must have at least
            // the full preamble to do so.

            if (block.data_offset() == 0
                
                // There is not much value in a 0-byte payload fragment so
                // discard those as well.
                || (block.data_length() != 0 &&
                    bundle.payload().length() == 0)
                
                // If the bundle should not be fragmented and the payload
                // block is not complete, we must discard the bundle.
                || bundle.do_not_fragment())
            {
                Log.d(TAG, "payload incomplete and cannot be fragmented");
                deletion_reason[0] = status_report_reason_t.REASON_BLOCK_UNINTELLIGIBLE;
                return false;
            }
        }

        return true;
    }
    
    /**
     * Produce a bundle by coping all payload and payload block metadata to buffer.
     * @param bundle Bundle to produce
     * @param block Payload block of Bundle
     * @param buf Copy the data on the current position this buffer
     * @param offset Offset of the block data. Keep track of how many bytes have been 
     * produced before. 
     * @param len Number of bytes to produce 
     */
    public void produce(final Bundle    bundle,
                 final BlockInfo block,
                 IByteBuffer          buf,
                 int           offset,
                 int           len){
    	
    	int old_position = buf.position();
        // First copy out the specified range of the preamble
        
    	if (offset < block.data_offset()) {
        	int tocopy = Math.min(len, block.data_offset() - offset);
        	
        	BufferHelper.copy_data(buf, buf.position(), block.contents(), offset, tocopy);
        	
        	buf.position(buf.position() + tocopy);
            offset += tocopy;
            len    -= tocopy;
        }

        if (len == 0){
        	buf.position(old_position);
        	return;
        }
            
        // Adjust offset to account for the preamble
        int payload_offset = offset - block.data_offset();
        
        int tocopy = (int) Math.min(len, bundle.payload().length() - payload_offset);
        bundle.payload().read_data(payload_offset, tocopy, buf);
        
    	buf.position(old_position);
        
        return;    	
    }
}
