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

public class PhgrepParser {

	//
	private static final int A_INIT = 2;

	//
	private static int split(String s, int p, boolean pos,
			PhgrepBuilder b) {
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
		b.appendAlternation(pos, z);
		return p;
	}

	/**
	 * 
	 * @param s
	 * @param b
	 */
	public static void parse(String s, PhgrepBuilder b) {
		int p, v;

		for(p = v = 0; p < s.length();) {
			if(s.charAt(p) == '(') {
				if(p > v)  b.appendString(s.substring(v, p));
				v = p = split(s, p, true, b) + 1;
			} else if(s.startsWith("-(", p) || s.startsWith("!(", p)) {
				if(p > v)  b.appendString(s.substring(v, p));
				v = p = split(s, p + 1, false, b) + 1;
			} else if(s.startsWith("  ")) {
				b.appendSpaces();
				while(s.charAt(p++) == ' ');
			} else {
				p++;
			}
		}
	}

}
