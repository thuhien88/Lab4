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

import automata.event.*;
import automata.turing.TuringMachine;
import java.awt.Point;
import java.io.Serializable;
import java.util.*;

/**
 * The automata object is the root class for the representation of all
 * forms of automata, including FSA, PDA, and Turing machines.  This
 * object does NOT simulate the behavior of any of those machines; it
 * simply maintains a structure that holds and maintains the data
 * necessary to represent such a machine.
 * 
 * @see automata.StateAutomaton
 * @see automata.Transition
 * 
 * @author Thomas Finley
 */

public class Automaton implements Serializable, Cloneable {
    /**
     * Creates an instance of <CODE>Automaton</CODE>.  The created
     * instance has no states and no transitions.
     */
    public Automaton() {
	states = new HashSet();
	transitions = new HashSet();
	finalStates = new HashSet();
	initialState = null;
    }

    /**
     * Creates a clone of this automaton.
     * @return a clone of this automaton, or <CODE>null</CODE> if the
     * clone failed
     */
    public Object clone() {
	Automaton a;
	// Try to create a new object.
	try {
	    // I am a bad person for writing this hack.
	    if (this instanceof TuringMachine)
		a = new TuringMachine(((TuringMachine)this).tapes());
	    else
		a = (Automaton) getClass().newInstance();
	} catch (Throwable e) {
	    // Well golly, we're sure screwed now!
	    System.err.println("Warning: clone of automaton failed!");
	    return null;
	}

	// Copy over the states.
	HashMap map = new HashMap(); // Old states to new states.
	Iterator it = states.iterator();
	while (it.hasNext()) {
	    StateAutomaton state = (StateAutomaton) it.next();
	    StateAutomaton nstate = new StateAutomaton(state.getID(),
				     new Point(state.getPoint()), a);
	    nstate.setLabel(state.getLabel());
	    map.put(state, nstate);
	    a.addState(nstate);
	}
	// Set special states.
	it = finalStates.iterator();
	while (it.hasNext()) {
	    StateAutomaton state = (StateAutomaton) it.next();
	    a.addFinalState((StateAutomaton) map.get(state));
	}
	a.setInitialState((StateAutomaton) map.get(getInitialState()));

	// Copy over the transitions.
	it = states.iterator();
	while (it.hasNext()) {
	    StateAutomaton state = (StateAutomaton) it.next();
	    Transition[] ts = getTransitionsFromState(state);
	    StateAutomaton from = (StateAutomaton) map.get(state);
	    for (int i=0; i<ts.length; i++) {
		StateAutomaton to = (StateAutomaton) map.get(ts[i].getToState());
		a.addTransition(ts[i].copy(from, to));
	    }
	}
	
	// Should be done now!
	return a;
    }

    /**
     * Retrieves all transitions that eminate from a state.
     * @param from the <CODE>State</CODE> from which returned
     * transitions should come from
     * @return an array of the <CODE>Transition</CODE> objects
     * emanating from this state
     */
    public Transition[] getTransitionsFromState(StateAutomaton from) {
	Transition[] toReturn = 
	    (Transition[]) transitionArrayFromStateMap.get(from); 
	if (toReturn == null) {
	    List list = (List) transitionFromStateMap.get(from);
	    toReturn = (Transition[]) list.toArray(new Transition[0]);
	    transitionArrayFromStateMap.put(from, toReturn);
	}
	return toReturn;
    }
    
    /**
     * Retrieves all transitions that travel from a state.
     * @param to the <CODE>State</CODE> to which all returned
     * transitions should go to
     * @return an array of all <CODE>Transition</CODE> objects going
     * to the State
     */
    public Transition[] getTransitionsToState(StateAutomaton to) {
	Transition[] toReturn = 
	    (Transition[]) transitionArrayToStateMap.get(to); 
	if (toReturn == null) {
	    List list = (List) transitionToStateMap.get(to);
	    toReturn = (Transition[]) list.toArray(new Transition[0]);
	    transitionArrayToStateMap.put(to, toReturn);
	}
	return toReturn;
    }

    /**
     * Retrieves all transitions going from one given state to another
     * given state.
     * @param from the state all returned transitions should come from
     * @param to the state all returned transitions should go to
     * @return an array of all transitions coming from
     * <CODE>from</CODE> and going to <CODE>to</CODE>
     */
    public Transition[] getTransitionsFromStateToState(StateAutomaton from, StateAutomaton to) {
	Transition[] t = getTransitionsFromState(from);
	ArrayList list = new ArrayList();
	for (int i=0; i<t.length; i++)    
	    if (t[i].getToState() == to)
		list.add(t[i]);
	return (Transition[]) list.toArray(new Transition[0]);
    }

    /**
     * Retrieves all transitions.
     * @return an array containing all transitions for this automaton
     */
    public Transition[] getTransitions() {
	if (cachedTransitions == null)
	    cachedTransitions = (Transition[])
		transitions.toArray(new Transition[0]);
	return cachedTransitions;
    }

    /**
     * Adds a <CODE>Transition</CODE> to this automaton.  This method
     * may do nothing if the transition is already in the automaton.
     * @param trans the transition object to add to the automaton
     */
    public void addTransition(Transition trans) {
	if (!getTransitionClass().isInstance(trans)) {
	    throw (new IncompatibleTransitionException());
	}
	if (transitions.contains(trans)) return;
	transitions.add(trans);
	List list = (List) transitionFromStateMap.get(trans.getFromState());
	list.add(trans);
	list = (List) transitionToStateMap.get(trans.getToState());
	list.add(trans);
 	transitionArrayFromStateMap.remove(trans.getFromState());
 	transitionArrayToStateMap.remove(trans.getToState());
	cachedTransitions = null;

	distributeTransitionEvent(new AutomataTransitionEvent(this, trans,
							      true, false));
    }

    /**
     * Replaces a <CODE>Transition</CODE> in this automaton with another
     * transition with the same from and to states.  This method
     * will delete the old if the transition is already in the automaton.
     * @param oldTrans the transition object to add to the automaton
     * @param newTrans the transition object to add to the automaton
     */
    public void replaceTransition(Transition oldTrans,
				  Transition newTrans) {
	if (!getTransitionClass().isInstance(newTrans)) {
	    throw new IncompatibleTransitionException();
	}
	if (oldTrans.equals(newTrans)) {
	    return;
	}
	if (transitions.contains(newTrans)) {
	    removeTransition(oldTrans);
	    return;
	}
	if (!transitions.remove(oldTrans)) {
	    throw new IllegalArgumentException
		("Replacing transition that not already in the automaton!");
	}
	transitions.add(newTrans);
	List list = (List) transitionFromStateMap.get(oldTrans.getFromState());
	list.set(list.indexOf(oldTrans), newTrans);
	list = (List) transitionToStateMap.get(oldTrans.getToState());
	list.set(list.indexOf(oldTrans), newTrans);
 	transitionArrayFromStateMap.remove(oldTrans.getFromState());
 	transitionArrayToStateMap.remove(oldTrans.getToState());
	cachedTransitions = null;
	distributeTransitionEvent
	    (new AutomataTransitionEvent(this, newTrans, true, false));
    }

    /**
     * Removes a <CODE>Transition</CODE> from this automaton.
     * @param trans the transition object to remove from this
     * automaton.
     */
    public void removeTransition(Transition trans) {
	transitions.remove(trans);
	List l = (List) transitionFromStateMap.get(trans.getFromState());
	l.remove(trans);
	l = (List) transitionToStateMap.get(trans.getToState());
	l.remove(trans);
	// Remove cached arrays.
	transitionArrayFromStateMap.remove(trans.getFromState());
	transitionArrayToStateMap.remove(trans.getToState());
	cachedTransitions = null;

	distributeTransitionEvent(new AutomataTransitionEvent(this, trans,
							      false, false));
    }

    /**
     * Creates a state, inserts it in this automaton, and returns that
     * state.  The ID for the state is set appropriately.
     * @param point the point to put the state at
     */
    public final StateAutomaton createState(Point point) {
	int i = 0;
	while (getStateWithID(i) != null) i++;
	StateAutomaton state = new StateAutomaton(i, point, this);
	addState(state);
	return state;
    }

    /**
     * Adds a new state to this automata.  Clients should use the
     * <CODE>createState</CODE> method instead.
     * @param state the state to add
     */
    protected final void addState(StateAutomaton state) {
	states.add(state);
	transitionFromStateMap.put(state, new LinkedList());
	transitionToStateMap.put(state, new LinkedList());
	cachedStates = null;
	distributeStateEvent(new AutomataStateEvent(this, state, true,
						    false, false));
    }
    
    /**
     * Removes a state from the automaton.  This will also remove all
     * transitions associated with this state.
     * @param state the state to remove
     */
    public void removeState(StateAutomaton state) {
	Transition[] t = getTransitionsFromState(state);
	for (int i=0; i<t.length; i++)
	    removeTransition(t[i]);
	t = getTransitionsToState(state);
	for (int i=0; i<t.length; i++)
	    removeTransition(t[i]);
	distributeStateEvent(new AutomataStateEvent(this, state,
						    false, false, false));
	states.remove(state);
	finalStates.remove(state);
	if (state == initialState) initialState = null;

	transitionFromStateMap.remove(state);
	transitionToStateMap.remove(state);
	
	transitionArrayFromStateMap.remove(state);
	transitionArrayToStateMap.remove(state);
	//cachedTransitions = null;

	cachedStates = null;
    }

    /**
     * Sets the new initial state to <CODE>initialState</CODE> and
     * returns what used to be the initial state, or <CODE>null</CODE>
     * if there was no initial state.  The state specified should
     * already exist in the automata.
     * @param initialState the new initial state
     * @return the old initial state, or <CODE>null</CODE> if there
     * was no initial state
     */
    public StateAutomaton setInitialState(StateAutomaton initialState) {
	StateAutomaton oldInitialState = this.initialState;
	this.initialState = initialState;
	return oldInitialState;
    }

    /**
     * Returns the start state for this automaton.
     * @return the start state for this automaton
     */
    public StateAutomaton getInitialState() {
	return this.initialState;
    }

    /**
     * Returns an array that contains every state in this automaton.
     * The array is gauranteed to be in order of ascending state IDs.
     * @return an array containing all the states in this automaton
     */
    public StateAutomaton[] getStates() {
	if (cachedStates == null) {
	    cachedStates = (StateAutomaton[]) states.toArray(new StateAutomaton[0]);
	    Arrays.sort(cachedStates, new Comparator() {
		    public int compare(Object o1, Object o2) {
			return ((StateAutomaton)o1).getID() - ((StateAutomaton)o2).getID();
		    }
		    public boolean equals(Object o) {
			return this==o;
		    } });
	}
	return cachedStates;
    }

    /**
     * Adds a single final state to the set of final states.  Note
     * that the general automaton can have an unlimited number of
     * final states, and should have at least one.  The state that is
     * added should already be one of the existing states.
     * @param finalState a new final state to add to the collection of
     * final states
     */
    public void addFinalState(StateAutomaton finalState) {
	cachedFinalStates = null;
	finalStates.add(finalState);
    }

    /**
     * Removes a state from the set of final states.  This will not
     * remove a state from the list of states; it shall merely make it
     * nonfinal.
     * @param state the state to make not a final state
     */
    public void removeFinalState(StateAutomaton state) {
	cachedFinalStates = null;
	finalStates.remove(state);
    }

    /**
     * Returns an array that contains every state in this automaton
     * that is a final state.  The array is not necessarily gauranteed
     * to be in any particular order.
     * @return an array containing all final states of this automaton
     */
    public StateAutomaton[] getFinalStates() {
	if (cachedFinalStates == null)
	    cachedFinalStates = (StateAutomaton[]) finalStates.toArray(new StateAutomaton[0]);
	return cachedFinalStates;
    }

    /**
     * Determines if the state passed in is in the set of final
     * states.
     * @param state the state to determine if is final
     * @return <CODE>true</CODE> if the state is a final state in
     * this automaton, <CODE>false</CODE> if it is not
     */
    public boolean isFinalState(StateAutomaton state) {
	return finalStates.contains(state);
    }

    /**
     * Returns the <CODE>State</CODE> in this automaton with this ID.
     * @param id the ID to look for
     * @return the instance of <CODE>State</CODE> in this automaton
     * with this ID, or <CODE>null</CODE> if no such state exists
     */
    public StateAutomaton getStateWithID(int id) {
	Iterator it = states.iterator();
	while (it.hasNext()) {
	    StateAutomaton state = (StateAutomaton) it.next();
	    if (state.getID() == id) return state;
	}
	return null;
    }
    
    /**
     * Tells if the passed in object is indeed a state in this
     * automaton.
     * @param state the state to check for membership in the automaton
     * @return <CODE>true</CODE> if this state is in the automaton,
     * <CODE>false</CODE>otherwise
     */
    public boolean isState(StateAutomaton state) {
	return states.contains(state);
    }

    /**
     * Returns the particular class that added transition objects
     * should be a part of.  Subclasses may wish to override in case
     * they want to restrict the type of transitions their automaton
     * will respect.  By default this method simply returns the class
     * object for the abstract class <CODE>automata.Transition</CODE>.
     * @see #addTransition
     * @see automata.Transition
     * @return the <CODE>Class</CODE> object that all added
     * transitions should derive from
     */
    protected Class getTransitionClass() {
	return automata.Transition.class;
    }

    /**
     * Returns a string representation of this <CODE>Automaton</CODE>.
     */
    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append(super.toString());
	buffer.append('\n');
	StateAutomaton[] states = getStates();
	for (int s=0; s<states.length; s++) {
	    if (initialState == states[s]) buffer.append("--> ");
	    buffer.append(states[s]);
	    if (isFinalState(states[s]))
		buffer.append(" **FINAL**");
	    buffer.append('\n');
	    Transition[] transitions = getTransitionsFromState(states[s]);
	    for (int t=0; t<transitions.length; t++) {
		buffer.append('\t');
		buffer.append(transitions[t]);
		buffer.append('\n');
	    }
	}

	return buffer.toString();
    }

    /**
     * Adds a <CODE>AutomataStateListener</CODE> to this automata.
     * @param listener the listener to add
     */
    public void addStateListener(AutomataStateListener listener) {
	stateListeners.add(listener);
    }

    /**
     * Adds a <CODE>AutomataTransitionListener</CODE> to this automata.
     * @param listener the listener to add
     */
    public void addTransitionListener(AutomataTransitionListener listener) {
	transitionListeners.add(listener);
    }

    /**
     * Gives an automata state change event to all state listeners.
     * @param event the event to distribute
     */
    void distributeStateEvent(AutomataStateEvent event) {
	Iterator it = stateListeners.iterator();
	while (it.hasNext()) {
	    AutomataStateListener listener =
		(AutomataStateListener) it.next();
	    listener.automataStateChange(event);
	}
    }

    /**
     * Removes a <CODE>AutomataStateListener</CODE> from this
     * automata.
     * @param listener the listener to remove
     */
    public void removeStateListener(AutomataStateListener listener) {
	stateListeners.remove(listener);
    }

    /**
     * Removes a <CODE>AutomataTransitionListener</CODE> from this
     * automata.
     * @param listener the listener to remove
     */
    public void removeTransitionListener(AutomataTransitionListener listener) {
	transitionListeners.remove(listener);
    }

    /**
     * Gives an automata transition change event to all transition
     * listeners.
     * @param event the event to distribute
     */
    void distributeTransitionEvent(AutomataTransitionEvent event) {
	Iterator it = transitionListeners.iterator();
	while (it.hasNext()) {
	    AutomataTransitionListener listener =
		(AutomataTransitionListener) it.next();
	    listener.automataTransitionChange(event);
	}
    }

    /**
     * This handles deserialization so that the listener sets are
     * reset to avoid null pointer exceptions when one tries to add
     * listeners to the object.
     * @param in the input stream for the object
     */
    private void readObject(java.io.ObjectInputStream in)
	throws java.io.IOException, ClassNotFoundException {
	// Reset all nonread objects.
	transitionListeners = new HashSet();
	stateListeners = new HashSet(); 
	transitionFromStateMap = new HashMap();
	transitionToStateMap = new HashMap();
	transitionArrayFromStateMap = new HashMap();
	transitionArrayToStateMap = new HashMap();
	transitions = new HashSet();
	states = new HashSet();

	// Do the reading in of objects.
	int version = in.readInt();
	if (version >= 0) { // Adjust by version.
	    // The reading for version 0 of this object.
	    Set s = (Set) in.readObject();
	    Iterator it = s.iterator();
	    while (it.hasNext()) addState((StateAutomaton) it.next());
	    initialState = (StateAutomaton) in.readObject();
	    finalStates = (Set) in.readObject();
	    // Let the class take care of the transition stuff.
	    Set trans = (Set) in.readObject();
	    it = trans.iterator();
	    while (it.hasNext()) addTransition((Transition) it.next());
	    if (this instanceof TuringMachine) {
		((TuringMachine)this).tapes = in.readInt();
	    }
	}
	if (version >= 1) {
	    
	}
	while (!in.readObject().equals("SENT")); // Read until sentinel.
    }

    /**
     * This handles serialization.
     */
    private void writeObject(java.io.ObjectOutputStream out)
	throws java.io.IOException {
	out.writeInt(0); // Version of the stream.
	// Version 0 outstuff...
	out.writeObject(states);
	out.writeObject(initialState);
	out.writeObject(finalStates);
	out.writeObject(transitions);
	if (this instanceof TuringMachine) {
	    out.writeInt(((TuringMachine)this).tapes);
	}
	out.writeObject("SENT"); // The sentinel object.
    }

    // AUTOMATA SPECIFIC CRAP
    // This includes lots of stuff not strictly necessary for the
    // defintion of automata, but stuff that makes it at least
    // somewhat efficient in the process.
    
    /** The collection of states in this automaton. */
    private Set states;
    /** The cached array of states. */
    private StateAutomaton[] cachedStates = null;
    /** The cached array of transitions. */
    private Transition[] cachedTransitions = null;
    /** The cached array of final states. */
    private StateAutomaton[] cachedFinalStates = null;

    /** The collection of final states in this automaton.  This is a
     * subset of the "states" collection. */
    private Set finalStates;
    /** The initial state. */
    private StateAutomaton initialState = null;

    /** The list of transitions in this automaton. */
    private Set transitions;
    /** A mapping from states to a list holding transitions from those
     * states. */
    private HashMap transitionFromStateMap = new HashMap();
    /** A mapping from state to a list holding transitions to those
     * states. */
    private HashMap transitionToStateMap = new HashMap();
    /** A mapping from states to an array holding transitions from
     * a state.  This is a sort of cashing. */
    private HashMap transitionArrayFromStateMap = new HashMap();
    /** A mapping from states to an array holding transitions from
     * a state.  This is a sort of cashing. */
    private HashMap transitionArrayToStateMap = new HashMap();

    // LISTENER STUFF 
    // Structures related to this object as something that generates
    // events, in particular as it pertains to the removal and
    // addition of states and transtions.
    private transient HashSet transitionListeners = new HashSet();
    private transient HashSet stateListeners = new HashSet(); 
}
