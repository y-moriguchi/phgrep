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

import java.util.Set;

public interface PhgrepState {

	/**
	 * 
	 * @return
	 */
	public Set<Integer> getLabels();

	/**
	 * 
	 * @param ch
	 * @return
	 */
	public PhgrepState getTransition(int ch);

	/**
	 * 
	 * @param ch
	 * @param s
	 */
	public void putTransition(int ch, PhgrepState s);

	/**
	 * 
	 * @param s
	 */
	public void putSpaceTrasition(PhgrepState s);

	/**
	 * 
	 * @param s
	 */
	public void putOtherTransition(PhgrepState s);

	/**
	 * 
	 * @param s
	 */
	public void putEmptyTransition(PhgrepState s);

	/**
	 * 
	 */
	public void markLastState();

}
