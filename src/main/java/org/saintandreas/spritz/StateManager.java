package org.saintandreas.spritz;

import static org.lwjgl.opengl.GL11.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class StateManager {
  private final Map<Integer, Stack<Boolean>> stateBits = new HashMap<>();
  // private final Stack<Program> programs = new Stack<>();

  private static final StateManager INSTANCE = new StateManager();

  public static StateManager get() {
    return INSTANCE;
  }

  protected Stack<Boolean> getStack(int state) {
    Stack<Boolean> result;
    if (!stateBits.containsKey(state)) {
      result = new Stack<>();
      result.push(glIsEnabled(state));
      stateBits.put(state, result);
    } else {
      result = stateBits.get(state);
    }
    return result;
  }

  public void pushEnable(int state, boolean enable) {
    Stack<Boolean> stack = getStack(state);
    assert (stack.size() > 1);
    if (enable != stack.peek()) {
      if (enable) {
        glEnable(state);
      } else {
        glDisable(state);
      }
    }
    stack.push(enable);
  }

  public void popEnable(int state) {
    Stack<Boolean> stack = getStack(state);
    assert (stack.size() > 1);
    boolean poppedValue = stack.pop();
    if (poppedValue != stack.peek()) {
      if (poppedValue) {
        glDisable(state);
      } else {
        glEnable(state);
      }
    }
  }

  // public void pushProgram(Program p) {
  // if (programs.peek() != p) {
  // p.use();
  // }
  // programs.push(p);
  // }
  //
  // public void popProgram() {
  // Program popped = programs.pop();
  // if (programs.isEmpty() || popped != programs.peek()) {
  // Program.clear();
  // }
  // }

  // public void pushViewport() {
  //
  // }
  //
  // public void popViewport(Program p) {
  // }
}
