/* -- JFLAP 4.0 --
 *
 * Copyright information:
 *
 * Susan H. Rodger, Thomas Finley
 * Computer Science Department
 * Duke University
 * April 24, 2003
 * Supported by National Science Foundation DUE-9752583.
 *
 * Copyright (c) 2003
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the author.  The name of the author may not be used to
 * endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
 
package automata;

import java.util.*;

/**
 * The useless states detector object can be used to find all states
 * in an automaton that either are not reachable from the initial
 * state, or that cannot reach a final state.  This does only naive
 * checking for the existence of edges.  It cannot be used, for
 * example, to solve the halting problem for a Turing machine.
 *
 * @author Thomas Finley
 */

public class UselessStatesDetector {
    /**
     * One can't create an instance of this.
     */
    private UselessStatesDetector() {
	 
    }

    /**
     * Returns a copy of an automaton that has all useless states removed.
     * @param a the automaton
     * @return a copy of the automaton with useless states removed
     */
    public static Automaton cleanAutomaton(Automaton a) {
	Automaton ac = (Automaton)a.clone();
	State[] s = ac.getStates();
	Set useless = getUselessStates(ac);
	for (int i=0; i<s.length; i++) {
	    if (useless.contains(s[i]) && s[i] != ac.getInitialState())
		ac.removeState(s[i]);
	}
	if (useless.contains(ac.getInitialState())) {
	    Transition[] t = ac.getTransitions();
	    for (int i=0; i<t.length; i++)
		ac.removeTransition(t[i]);
	}
	return ac;
    }
    
    /**
     * Returns all states in automaton that are useless.
     * @param a the automaton to find useless states
     * @return a set containing all states in the automaton that are
     * unreachable from the initial state or cannot lead to a final
     * state
     * @throws IllegalArgumentException if the automata does not have
     * an initial state
     */
    public static Set getUselessStates(Automaton a) {
	 if (a.getInitialState() == null) {
	     throw new IllegalArgumentException
		 ("Automata does not have an initial state!");
	 }
	 Set finalized = findFinal(a);
	 Set initialized = findInitial(a);
	 Set useless = new HashSet(Arrays.asList(a.getStates()));
	 finalized.retainAll(initialized);
	 useless.removeAll(finalized);
	 return useless;
    }
    
    /**
     * Find all states that can lead to a final state.
     * @param a the automaton
     * @return the set of state that can lead to a final state
     */
    private static Set findFinal(Automaton a) {
	Set finalized = new HashSet();
	finalized.addAll(Arrays.asList(a.getFinalStates()));
	boolean added = finalized.size() != 0;
	Transition[] t = a.getTransitions();
	while (added) {
	    added = false;
	    for (int i=0; i<t.length; i++)
		if (finalized.contains(t[i].getToState()))
		    added = added || finalized.add(t[i].getFromState());
	}
	return finalized;
    }

    /**
     * Find all states reachable from an initial state.
     * @param a the automaton
     * @return the set of states reachable from an initial state
     */
    private static Set findInitial(Automaton a) {
	Set initialized = new HashSet();
	initialized.add(a.getInitialState());
	boolean added = true;
	Transition[] t = a.getTransitions();
	while (added) {
	    added = false;
	    for (int i=0; i<t.length; i++)
		if (initialized.contains(t[i].getFromState()))
		    added = added || initialized.add(t[i].getToState());
	}
	return initialized;
    }
}
