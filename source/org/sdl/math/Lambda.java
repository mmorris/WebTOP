/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Lambda.java
//Declares classes for representing pure (lambda) functions.
//Davis Herring
//Created November 10 2002
//Updated November 13 2002
//Version 1.01

package org.sdl.math;

import java.lang.reflect.*;

//Functions here that use arrays do not do bounds checking.
//The point of instantiating instances of Lambda or its inner classes is that
//the implementing function (impl) can be later changed, causing all objects
//using the Lambda object to get the changed behavior.

public class Lambda implements Function
{
	public static class TwoVar implements Function.TwoVar {
		public Function.TwoVar impl;
		public TwoVar(Function.TwoVar f) {impl=f;}
		public double eval(double arg1,double arg2) {return impl.eval(arg1,arg2);}

		public static Function.TwoVar compose(final Function outer,final Function.TwoVar inner) {
			return new Function.TwoVar() {
				public double eval(double arg1,double arg2) {return outer.eval(inner.eval(arg1,arg2));}
			};
		}
		public static Function.TwoVar composeLeft(final Function.TwoVar outer,final Function inner) {
			return new Function.TwoVar() {
				public double eval(double arg1,double arg2) {return outer.eval(inner.eval(arg1),arg2);}
			};
		}
		public static Function.TwoVar composeRight(final Function.TwoVar outer,final Function inner) {
			return new Function.TwoVar() {
				public double eval(double arg1,double arg2) {return outer.eval(arg1,inner.eval(arg2));}
			};
		}
		public static Function.TwoVar compose(final Function.TwoVar outer,final Function left,final Function right) {
			return new Function.TwoVar() {
				public double eval(double arg1,double arg2) {return outer.eval(left.eval(arg1),right.eval(arg2));}
			};
		}
		public static Function.TwoVar compose(final Function.TwoVar outer,final Function.TwoD inner) {
			return new Function.TwoVar() {
				public double eval(double arg1,double arg2) {
					double[] args=inner.eval(arg1,arg2);
					return outer.eval(args[0],args[1]);
				}
			};
		}

		public static Function.TwoVar simplifyLeft(final Function.ThreeVar outer,final double inner) {
			return new Function.TwoVar() {
				public double eval(double arg1,double arg2) {
					return outer.eval(inner,arg1,arg2);
				}
			};
		}
		public static Function.TwoVar simplifyMiddle(final Function.ThreeVar outer,final double inner) {
			return new Function.TwoVar() {
				public double eval(double arg1,double arg2) {
					return outer.eval(arg1,inner,arg2);
				}
			};
		}
		public static Function.TwoVar simplifyRight(final Function.ThreeVar outer,final double inner) {
			return new Function.TwoVar() {
				public double eval(double arg1,double arg2) {
					return outer.eval(arg1,arg2,inner);
				}
			};
		}

		public static Function.TwoVar fromMethod(Class<?> c,String name) {
			try {
				final Method m=c.getMethod(name,new Class[] {double.class,double.class});
				if(m.getReturnType()==double.class && Modifier.isStatic(m.getModifiers()))
				return new Function.TwoVar() {
					public double eval(double arg1,double arg2) {
						try {
							return ((Double)m.invoke(null,new Object[] {new Double(arg1),new Double(arg2)})).doubleValue();
						} catch(IllegalAccessException e) {throw new RuntimeException(e.toString());}
						catch(InvocationTargetException e) {throw new RuntimeException(e.toString());}
					}
				};
			} catch(NoSuchMethodException e) {}
			throw new IllegalArgumentException("Invalid method signature [need static double _(double,double)].");
		}
	}
	public static class ThreeVar implements Function.ThreeVar {
		public Function.ThreeVar impl;
		public ThreeVar(Function.ThreeVar f) {impl=f;}
		public double eval(double arg1,double arg2,double arg3) {return impl.eval(arg1,arg2,arg3);}

		public static Function.ThreeVar compose(final Function outer,final Function.ThreeVar inner) {
			return new Function.ThreeVar() {
				public double eval(double arg1,double arg2,double arg3) {return outer.eval(inner.eval(arg1,arg2,arg3));}
			};
		}
		public static Function.ThreeVar composeLeft(final Function.ThreeVar outer,final Function inner) {
			return new Function.ThreeVar() {
				public double eval(double arg1,double arg2,double arg3) {return outer.eval(inner.eval(arg1),arg2,arg3);}
			};
		}
		public static Function.ThreeVar composeMiddle(final Function.ThreeVar outer,final Function inner) {
			return new Function.ThreeVar() {
				public double eval(double arg1,double arg2,double arg3) {return outer.eval(arg1,inner.eval(arg2),arg3);}
			};
		}
		public static Function.ThreeVar composeRight(final Function.ThreeVar outer,final Function inner) {
			return new Function.ThreeVar() {
				public double eval(double arg1,double arg2,double arg3) {return outer.eval(arg1,arg2,inner.eval(arg3));}
			};
		}
		//To compose two slots, compose all three and use Function.identity for one.
		public static Function.ThreeVar compose(final Function.ThreeVar outer,final Function left,final Function middle,final Function right) {
			return new Function.ThreeVar() {
				public double eval(double arg1,double arg2,double arg3) {return outer.eval(left.eval(arg1),middle.eval(arg2),right.eval(arg3));}
			};
		}
		public static Function.ThreeVar compose(final Function.ThreeVar outer,final Function.ThreeD inner) {
			return new Function.ThreeVar() {
				public double eval(double arg1,double arg2,double arg3) {
					double[] args=inner.eval(arg1,arg2,arg3);
					return outer.eval(args[0],args[1],args[2]);
				}
			};
		}

		public static Function.ThreeVar fromMethod(Class<?> c,String name) {
			try {
				final Method m=c.getMethod(name,new Class[] {double.class,double.class,double.class});
				if(m.getReturnType()==double.class && Modifier.isStatic(m.getModifiers()))
				return new Function.ThreeVar() {
					public double eval(double arg1,double arg2,double arg3) {
						try {
							return ((Double)m.invoke(null,new Object[] {new Double(arg1),new Double(arg2),new Double(arg3)})).doubleValue();
						} catch(IllegalAccessException e) {throw new RuntimeException(e.toString());}
						catch(InvocationTargetException e) {throw new RuntimeException(e.toString());}
					}
				};
			} catch(NoSuchMethodException e) {}
			throw new IllegalArgumentException("Invalid method signature [need static double _(double,double,double)].");
		}
	}
	public static class NVar implements Function.NVar {
		public Function.NVar impl;
		public NVar(Function.NVar f) {impl=f;}
		public double eval(double[] args) {return impl.eval(args);}

		public static Function.NVar compose(final Function outer,final Function.NVar inner) {
			return new Function.NVar() {
				public double eval(double[] args) {return outer.eval(inner.eval(args));}
			};
		}
		public static Function.NVar composeOne(final Function.NVar outer,final Function inner,final int position) {
			return new Function.NVar() {
				public double eval(double[] args) {
					double[] arguments=(double[])args.clone();
					arguments[position]=inner.eval(arguments[position]);
					return outer.eval(arguments);
				}
			};
		}
		public static Function.NVar compose(final Function.NVar outer,Function[] inner) {
			final Function[] inners=(Function[])inner.clone();
			return new Function.NVar() {
				public double eval(double[] args) {
					double[] arguments=(double[])args.clone();
					for(int index=0;index<arguments.length;++index)
						arguments[index]=inners[index].eval(arguments[index]);
					return outer.eval(arguments);
				}
			};
		}
		public static Function.NVar compose(final Function.NVar outer,final Function.ND inner) {
			return new Function.NVar() {
				public double eval(double[] args) {
					return outer.eval(inner.eval(args));
				}
			};
		}

		public static Function.NVar simplifyOne(final Function.NVar outer,final double inner,final int position) {
			return new Function.NVar() {
				public double eval(double[] args) {
					double[] arguments=(double[])args.clone();
					arguments[position]=inner;
					return outer.eval(arguments);
				}
			};
		}
	}
	public static class TwoD implements Function.TwoD {
		public Function.TwoD impl;
		public TwoD(Function.TwoD f) {impl=f;}
		public double[] eval(double arg1,double arg2) {return impl.eval(arg1,arg2);}

		public static Function.TwoD composeLeft(final Function.TwoD outer,final Function inner) {
			return new Function.TwoD() {
				public double[] eval(double arg1,double arg2) {return outer.eval(inner.eval(arg1),arg2);}
			};
		}
		public static Function.TwoD composeRight(final Function.TwoD outer,final Function inner) {
			return new Function.TwoD() {
				public double[] eval(double arg1,double arg2) {return outer.eval(arg1,inner.eval(arg2));}
			};
		}
		public static Function.TwoD compose(final Function.TwoD outer,final Function left,final Function right) {
			return new Function.TwoD() {
				public double[] eval(double arg1,double arg2) {return outer.eval(left.eval(arg1),right.eval(arg2));}
			};
		}
		public static Function.TwoD compose(final Function.TwoD outer,final Function.TwoD inner) {
			return new Function.TwoD() {
				public double[] eval(double arg1,double arg2) {
					double[] args=inner.eval(arg1,arg2);
					return outer.eval(args[0],args[1]);
				}
			};
		}

		public static Function.TwoD simplifyLeft(final Function.ThreeD outer,final double inner) {
			return new Function.TwoD() {
				public double[] eval(double arg1,double arg2) {
					return outer.eval(inner,arg1,arg2);
				}
			};
		}
		public static Function.TwoD simplifyMiddle(final Function.ThreeD outer,final double inner) {
			return new Function.TwoD() {
				public double[] eval(double arg1,double arg2) {
					return outer.eval(arg1,inner,arg2);
				}
			};
		}
		public static Function.TwoD simplifyRight(final Function.ThreeD outer,final double inner) {
			return new Function.TwoD() {
				public double[] eval(double arg1,double arg2) {
					return outer.eval(arg1,arg2,inner);
				}
			};
		}

		public static Function.TwoD parallel(final Function left,final Function right) {
			return new Function.TwoD() {
				public double[] eval(double arg1,double arg2) {
					return new double[] {left.eval(arg1),right.eval(arg2)};
				}
			};
		}
	}
	public static class ThreeD implements Function.ThreeD {
		public Function.ThreeD impl;
		public ThreeD(Function.ThreeD f) {impl=f;}
		public double[] eval(double arg1,double arg2,double arg3) {return impl.eval(arg1,arg2,arg3);}

		public static Function.ThreeD compose(final Function.ThreeD outer,final Function left,final Function middle,final Function right) {
			return new Function.ThreeD() {
				public double[] eval(double arg1,double arg2,double arg3) {
					return outer.eval(left.eval(arg1),middle.eval(arg2),right.eval(arg3));
				}
			};
		}
		public static Function.ThreeD compose(final Function.ThreeD outer,final Function.ThreeD inner) {
			return new Function.ThreeD() {
				public double[] eval(double arg1,double arg2,double arg3) {
					double[] args=inner.eval(arg1,arg2,arg3);
					return outer.eval(args[0],args[1],args[2]);
				}
			};
		}

		public static Function.ThreeD composeLeft(final Function.ThreeD outer,final Function inner) {
			return new Function.ThreeD() {
				public double[] eval(double arg1,double arg2,double arg3) {return outer.eval(inner.eval(arg1),arg2,arg3);}
			};
		}
		public static Function.ThreeD composeMiddle(final Function.ThreeD outer,final Function inner) {
			return new Function.ThreeD() {
				public double[] eval(double arg1,double arg2,double arg3) {return outer.eval(arg1,inner.eval(arg2),arg3);}
			};
		}
		public static Function.ThreeD composeRight(final Function.ThreeD outer,final Function inner) {
			return new Function.ThreeD() {
				public double[] eval(double arg1,double arg2,double arg3) {return outer.eval(arg1,arg2,inner.eval(arg3));}
			};
		}

		public static Function.ThreeD parallel(final Function left,final Function middle,final Function right) {
			return new Function.ThreeD() {
				public double[] eval(double arg1,double arg2,double arg3) {
					return new double[] {left.eval(arg1),middle.eval(arg2),right.eval(arg3)};
				}
			};
		}
	}
	public static class ND implements Function.ND {
		public Function.ND impl;
		public ND(Function.ND f) {impl=f;}
		public double[] eval(double[] args) {return impl.eval(args);}

		public static Function.ND compose(final Function.ND outer,Function[] inner) {
			final Function[] inners=(Function[])inner.clone();
			return new Function.ND() {
				public double[] eval(double[] args) {
					double[] rets=new double[inners.length];
					for(int index=0;index<inners.length;++index)
						rets[index]=inners[index].eval(args[index]);
					return outer.eval(rets);
				}
			};
		}
		public static Function.ND composeOne(final Function.ND outer,final Function inner,final int position) {
			return new Function.ND() {
				public double[] eval(double[] args) {
					double[] arguments=(double[])args.clone();
					arguments[position]=inner.eval(arguments[position]);
					return outer.eval(arguments);
				}
			};
		}
		public static Function.ND compose(final Function.ND outer,final Function.ND inner) {
			return new Function.ND() {
				public double[] eval(double[] args) {
					return outer.eval(inner.eval(args));
				}
			};
		}

		public static Function.ND simplifyOne(final Function.ND outer,final double inner,final int position) {
			return new Function.ND() {
				public double[] eval(double[] args) {
					double[] arguments=(double[])args.clone();
					arguments[position]=inner;
					return outer.eval(arguments);
				}
			};
		}

		public static Function.ND parallel(Function[] funcs) {
			//defensive copying -- yay
			final Function[] functions=(Function[])funcs.clone();
			return new Function.ND() {
				public double[] eval(double[] args) {
					double[] ret=new double[args.length];
					for(int index=0;index<args.length;++index)
						ret[index]=functions[index].eval(args[index]);
					return ret;
				}
			};
		}
	}

	public Function impl;
	public Lambda(Function f) {impl=f;}
	public double eval(double arg) {return impl.eval(arg);}

	public static Function constant(final double c) {
		return new Function() {
			public double eval(double arg) {return c;}
		};
	}
	public static Function linear(final double m,final double b) {
		return new Function() {
			public double eval(double arg) {return m*arg+b;}
		};
	}
	//First element is constant term
	public static Function polynomial(double[] coeffs) {
		//defensive copying -- yay
		final double[] c=(double[])coeffs.clone();
		return new Function() {
			public double eval(double arg) {
				double power=1,ret=0;
				for(int index=0;index<c.length;++index,power*=arg)
					ret+=c[index]*power;
				return ret;
			}
		};
	}

	public static Function compose(final Function outer,final Function inner) {
		return new Function() {
			public double eval(double arg) {
				return outer.eval(inner.eval(arg));
			}
		};
	}

	public static Function simplifyLeft(final Function.TwoVar outer,final double inner) {
		return new Function() {
			public double eval(double arg) {
				return outer.eval(inner,arg);
			}
		};
	}
	public static Function simplifyRight(final Function.TwoVar outer,final double inner) {
		return new Function() {
			public double eval(double arg) {
				return outer.eval(arg,inner);
			}
		};
	}

	public static Function simplifyToLeft(final Function.ThreeVar outer,final double middle,final double right) {
		return new Function() {
			public double eval(double arg) {
				return outer.eval(arg,middle,right);
			}
		};
	}
	public static Function simplifyToMiddle(final Function.ThreeVar outer,final double left,final double right) {
		return new Function() {
			public double eval(double arg) {
				return outer.eval(left,arg,right);
			}
		};
	}
	public static Function simplifyToRight(final Function.ThreeVar outer,final double left,final double middle) {
		return new Function() {
			public double eval(double arg) {
				return outer.eval(left,middle,arg);
			}
		};
	}

	//The array's value at position will be replaced with the argument to the Function
	public static Function simplifyToOne(final Function.NVar outer,double[] inner,final int position) {
		//defensive copying -- yay
		final double[] inners=(double[])inner.clone();
		return new Function() {
			public double eval(double arg) {
				inners[position]=arg;
				return outer.eval(inners);
			}
		};
	}

	public static Function fromMethod(Class<?> c,String name) {
		try {
			final Method m=c.getMethod(name,new Class[] {double.class});
			if(m.getReturnType()==double.class && Modifier.isStatic(m.getModifiers()))
				return new Function() {
					public double eval(double arg) {
						try {
							return ((Double)m.invoke(null,new Object[] {new Double(arg)})).doubleValue();
						} catch(IllegalAccessException e) {throw new RuntimeException(e.toString());}
						catch(InvocationTargetException e) {throw new RuntimeException(e.toString());}
					}
				};
		} catch(NoSuchMethodException e) {}
		throw new IllegalArgumentException("Invalid method signature [need static double _(double)].");
	}
}
