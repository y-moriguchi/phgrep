/*
 * Copyright 2014 Yuichiro Moriguchi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.morilib.phgrep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhgrepParser {

	//
	private static class Block {

		private boolean spaces = false;
		private List<String> negative = new ArrayList<String>();
		private List<String> positive = new ArrayList<String>();

		boolean hasPositive() {
			return positive.size() > 0 || spaces;
		}

		boolean hasNegative() {
			return negative.size() > 0;
		}

	}

	//
	private static final int A_INIT = 2;

	//
	private PhgrepStateFactory factory;
	private transient List<Block> blocks;
	private transient Block now;

	/**
	 * 
	 * @param f
	 */
	public PhgrepParser(PhgrepStateFactory f) {
		factory = f;
	}

	//
	private void appendAlternation(boolean pos, String[] z) {
		if(pos) {
			now.positive.addAll(Arrays.asList(z));
			blocks.add(now);
			now = new Block();
		} else {
			now.negative.addAll(Arrays.asList(z));
		}
	}

	//
	private void appendSpaces() {
		now.spaces = true;
		blocks.add(now);
		now = new Block();
	}

	//
	private void appendString(String s) {
		now.positive.add(s);
		blocks.add(now);
		now = new Block();
	}

	//
	private int split(String s, int p, boolean pos) {
		String[] a, z;
		int k, v, w;

		v = p;
		if((p = s.indexOf(')', p + 1)) < 0) {
			throw new IllegalArgumentException();
		}

		a = new String[A_INIT];
		for(k = 0; v < p;) {
			if(k >= a.length) {
				z = a;
				a = new String[a.length * 2];
				System.arraycopy(z, 0, a, 0, z.length);
			}
			w = v;
			v = (v = s.indexOf('|', v + 1)) < 0 ? p : v;
			a[k++] = s.substring(w, v);
		}

		z = new String[k];
		System.arraycopy(a, 0, z, 0, k);
		appendAlternation(pos, z);
		return p;
	}

	//
	private void buildTrie(PhgrepState s, List<String> l, int p,
			boolean pos, List<PhgrepState> ss) {
		PhgrepState r;

		for(String t : l) {
			r = factory.newInstance();
			if(p < t.length()) {
				if(!pos) {
					ss.add(s);
				}
				s.putTransition((int)t.charAt(p), r);
				buildTrie(r, l, p + 1, pos, ss);
			} else if(pos) {
				ss.add(s);
			}
		}
	}

	//
	private PhgrepState buildMachine() {
		PhgrepState n, p, v, x = null, r = null;
		List<PhgrepState> ln, lp;

		for(Block b : blocks) {
			n = p = null;  ln = lp = null;
			if(b.hasNegative()) {
				n  = factory.newInstance();
				ln = new ArrayList<PhgrepState>();
				buildTrie(n, b.negative, 0, false, ln);
			}

			if(b.spaces) {
				p  = factory.newInstance();
				v  = factory.newInstance();
				lp = new ArrayList<PhgrepState>();
				p.putSpaceTrasition(v);
				v.putSpaceTrasition(v);
				lp.add(v);
			} else if(b.hasPositive()) {
				p  = factory.newInstance();
				lp = new ArrayList<PhgrepState>();
				buildTrie(n, b.positive, 0, true, lp);
			}

			if(r == null) {
				r = n != null ? n : p;
			} else {
				x.putEmptyTransition(n != null ? n : p);
			}

			x = factory.newInstance();
			if(p == null) {
				if(n == null)  throw new RuntimeException();
				for(PhgrepState a : ln) {
					a.putEmptyTransition(x);
				}
			} else if(n == null) {
				for(PhgrepState a : lp) {
					a.putEmptyTransition(x);
				}
			} else {
				for(PhgrepState a : ln) {
					a.putEmptyTransition(p);
				}
				p.putOtherTransition(p);
				for(PhgrepState a : lp) {
					a.putEmptyTransition(x);
				}
			}
		}
		x.markLastState();
		return r;
	}

	/**
	 * 
	 * @param s
	 * @param b
	 */
	public PhgrepState parse(String s) {
		int p, v;

		blocks = new ArrayList<Block>();
		now = new Block();
		for(p = v = 0; p < s.length();) {
			if(s.charAt(p) == '(') {
				if(p > v)  appendString(s.substring(v, p));
				v = p = split(s, p, true) + 1;
			} else if(s.startsWith("-(", p) || s.startsWith("!(", p)) {
				if(p > v)  appendString(s.substring(v, p));
				v = p = split(s, p + 1, false) + 1;
			} else if(s.startsWith("  ")) {
				appendSpaces();
				while(s.charAt(p++) == ' ');
			} else {
				p++;
			}
		}
		return buildMachine();
	}

}
