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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockInfo;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockInfoVec;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.Bundle;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.contacts.Link;


/**
 * This class represents the Security Policy Database (SPD), which contains a collection of rules instructing 
 * what policy should be applied to packets received and sent by the device. In the current DTN2.6 implementation, it
 * only supports global SPD for incoming and outgoing secured bundles. It contains:
 *   - global CB on/off setting
 *   - public keys for PSB and CB"[DTN2]
 *   
 *  @author Sebastian Domancich (sdo@kth.se)
 */
public class SPD 
{
	private static SPD instance_ = null;

	/**
	 * String TAG to support Android logging system
	 */
	private static String TAG = "SPD";

	private static spd_policy_t global_policy_inbound_;
	private static spd_policy_t global_policy_outbound_;

	public static SPD getInstance() 
	{
		if (instance_ == null) 
		{
			instance_ = new SPD();

		}
		return instance_;
	}
	// End Singleton Implementation of the DTNScheme 

	public SPD()
	{
		global_policy_inbound_=spd_policy_t.SPD_USE_NONE;
		global_policy_outbound_ = spd_policy_t.SPD_USE_NONE;
	}

	public enum spd_direction_t
	{
		SPD_DIR_IN(1),
		SPD_DIR_OUT(2);

		private static final Map<Integer, spd_direction_t> lookup = new HashMap<Integer, spd_direction_t>();

		static {
			for (spd_direction_t s : EnumSet.allOf(spd_direction_t.class))
				lookup.put(s.getCode(), s);
		}

		private int code;

		private spd_direction_t(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static spd_direction_t get(int code) {
			return lookup.get(code);
		}

	}	

	public enum spd_policy_t
	{
		SPD_USE_NONE  (0),      // (bin) 000 == (dec) 0

		SPD_USE_BAB   (1 << 0), // (bin) 001 == (dec) 1
		SPD_USE_CB    (1 << 1), // (bin) 010 == (dec) 2
		SPD_USE_PSB   ( 1 << 2),// (bin) 100 == (dec) 3
	
		PSD_USE_BAB_CB (0x03),  //011
		PSD_USE_BAB_PSB (0x05), //101
		
		PSD_USE_CB_PSB (0x06),//110
		
		PSD_USE_BAB_CB_PSB (0x07); //111

		private static final Map<Integer, spd_policy_t> lookup = new HashMap<Integer, spd_policy_t>();

		static {
			for (spd_policy_t s : EnumSet.allOf(spd_policy_t.class))
				lookup.put(s.getCode(), s);
		}

		private int code;

		private spd_policy_t(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static spd_policy_t get(int code) {
			return lookup.get(code);
		}
	}

	/**
	 * Boot time initializer.
	 */
	public static void init()
	{
		Log.d(TAG, "Going to init" );
		Log.d(TAG, "init done." );
	}

	public spd_policy_t get_global_policy(spd_direction_t direction)
	{
		if (direction.equals(spd_direction_t.SPD_DIR_IN))
			return global_policy_inbound_;
		else
			return global_policy_outbound_;
	}
	/**
	 * Set global policy to a bitwise-OR'ed combination of
	 * SPD_USE_BAB, SPD_USE_PSB, and/or SPD_USE_CB.  SPD_USE_NONE can
	 * also be specified to turn security features off entirely.
	 */
	public  String set_global_policy(spd_direction_t direction,
			spd_policy_t policy)
	{
		assert(direction == spd_direction_t.SPD_DIR_IN || direction == spd_direction_t.SPD_DIR_OUT);
		//
		//assert((policy & ~ (spd_policy_t.SPD_USE_BAB | spd_policy_t.SPD_USE_CB | spd_policy_t.SPD_USE_PSB))==0);
		if (direction.equals(spd_direction_t.SPD_DIR_IN))
			global_policy_inbound_=policy;
		else
			global_policy_outbound_=policy;
		Log.d(TAG, "SPD.set_global_policy() done" );
		return policy.toString();
	}

	/**
	 * Add the security blocks required by security policy for the
	 * given outbound bundle.
	 */
	public void prepare_out_blocks(Bundle bundle, Link link, BlockInfoVec xmit_blocks)
	{
		spd_policy_t policy = find_policy (spd_direction_t.SPD_DIR_OUT, bundle);
		int err=0;


		if ((policy.getCode() & spd_policy_t.SPD_USE_PSB.getCode())>0) 
		{
			Ciphersuite bp = Ciphersuite.find_suite(Ciphersuite_PS2.CSNUM_PS2);
			assert(bp != null);
			err=bp.prepare(bundle, xmit_blocks, null, link, BlockInfo.list_owner_t.LIST_NONE);   
		}


		if ((policy.getCode() & spd_policy_t.SPD_USE_CB.getCode())>0) 
		{
			Ciphersuite bp =  Ciphersuite.find_suite(Ciphersuite_C3.CSNUM_C3);
			assert(bp != null);
			err=bp.prepare(bundle, xmit_blocks, null, link, BlockInfo.list_owner_t.LIST_NONE);
		}

		if ((policy.getCode() & spd_policy_t.SPD_USE_BAB.getCode())>0) 
		{
			Ciphersuite bp = Ciphersuite.find_suite(Ciphersuite_BA1.CSNUM_BA1);
			assert(bp != null);
			err=bp.prepare(bundle, xmit_blocks, null, link, BlockInfo.list_owner_t.LIST_NONE);
		}
		if (err!=0)
			Log.e(TAG, String.format("SPD.prepare_out_blocks(): ERROR WHILE EXECUTING *****************************"));
		else
			Log.d(TAG, String.format("SPD.prepare_out_blocks() done"));	
	}

	/**
	 * Check whether sequence of BP_Tags created during input processing
	 * meets the security policy for this bundle.
	 */
	public boolean verify_in_policy( Bundle bundle)
	{
		spd_policy_t policy = find_policy(spd_direction_t.SPD_DIR_IN, bundle);
		BlockInfoVec recv_blocks = bundle.recv_blocks();

		Log.d(TAG, String.format("SPD::verify_in_policy() 0x%x", policy.getCode()));
		Log.e(TAG, String.format("----------------------------------------------------"));
		//Log.d(TAG, String.format("SPD.verify_in_policy() 0x"+policy.getCode()));

		if ((policy.getCode() & spd_policy_t.SPD_USE_BAB.getCode())>0) {
			if ( !Ciphersuite.check_validation(bundle, recv_blocks, Ciphersuite_BA1.CSNUM_BA1 )) {
				Log.d(TAG, String.format("SPD::verify_in_policy() no BP_TAG_BAB_IN_DONE"));
				return false;
			}
		}

		if ((policy.getCode() & spd_policy_t.SPD_USE_CB.getCode())>0) {
			if ( !Ciphersuite.check_validation(bundle, recv_blocks, Ciphersuite_C3.CSNUM_C3 )) {
				Log.d(TAG, String.format("SPD::verify_in_policy() no BP_TAG_CB_IN_DONE"));
				return false;
			}
		}

		if ((policy.getCode() & spd_policy_t.SPD_USE_PSB.getCode())>0) 
		{
			if ( !Ciphersuite.check_validation(bundle, recv_blocks, Ciphersuite_PS2.CSNUM_PS2 )) 
			{
				Log.d(TAG, String.format("SPD::verify_in_policy() no BP_TAG_PSB_IN_DONE"));
				return false;
			}
		}

		return true;
	}

	/**
	 * Return the policy for the given bundle in the given direction.
	 *
	 * XXX For now this just returns the global policy regardless of
	 * the value of the 'bundle' argument; in the future it should be
	 * modified to look up an SPD entry indexed by source and
	 * destination EndpointIDPatterns.
	 */
	static spd_policy_t find_policy(spd_direction_t direction,
			Bundle bundle)
	{
		assert(direction == spd_direction_t.SPD_DIR_IN || direction == spd_direction_t.SPD_DIR_OUT);

		//(void)bundle;
		Log.d(TAG, String.format(" SPD::find_policy()"));

		return (direction == spd_direction_t.SPD_DIR_IN ? global_policy_inbound_: global_policy_outbound_);
	}
} 