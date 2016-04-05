package org.yamaLab.pukiwikiCommunicator;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.yamaLab.pukiwikiCommunicator.language.ALisp;
import org.yamaLab.pukiwikiCommunicator.language.ABasic;
import org.yamaLab.pukiwikiCommunicator.language.CQueue;
import org.yamaLab.pukiwikiCommunicator.language.InterpreterInterface;
import org.yamaLab.pukiwikiCommunicator.language.LispObject;
import org.yamaLab.pukiwikiCommunicator.language.MyDouble;
import org.yamaLab.pukiwikiCommunicator.language.MyInt;
import org.yamaLab.pukiwikiCommunicator.language.MyLong;
import org.yamaLab.pukiwikiCommunicator.language.MyNumber;
import org.yamaLab.pukiwikiCommunicator.language.MyString;
//import org.yamaLab.pukiwikiCommunicator.language.OutputMessageHandler;
import org.yamaLab.pukiwikiCommunicator.language.ReadLine;
import org.yamaLab.pukiwikiCommunicator.language.Util;

public class Basic extends ABasic implements InterpreterInterface
{
	Hashtable waitingTable;
    public LispObject evaluatingList;
    String results="";
    String waitingFunction="";
    static String TAG="Basic";
    boolean running=false;
    
    public void setWaitingFunction(String s){
    	this.waitingFunction=s;
    }
    private void runProgram(String input){
//		textOutput.appendText("> "+input+"\n");
        this.clearEnvironment();
//        CQueue inqueue2=new CQueue();
		inqueue.putString(input);
		LispObject o=((ReadLine)(read)).readProgram(inqueue);
        LispObject p=this.basicparser.parseBasic(o);
//        String px=print.print(p);
//        printMessage(px);
        this.evalList(p);
    }

    public String getResult(){
    	return results;
    }
    public void setResult(String x){
    	this.results=x;
        synchronized(waitingTable){
     	   this.waitingTable.put(waitingFunction,results);
        }
        this.waitingFunction="";
//    	System.out.println("results="+results);
    }
    public LispObject applyMiscOperation(LispObject proc, LispObject argl)
    {
        LispObject x=super.applyMiscOperation(proc,argl);
        if(x!=null) return x;
        if(eq(proc,recSymbol("ex"))){
                String appliname=print.print(car(argl));
                String command=print.print(second(argl));
                InterpreterInterface appli=this.lookUp(appliname);
                String rtn=appli.parseCommand(command);
                if(rtn==null){
                	return new MyString("ERROR");
                }
                MyString val=new MyString(rtn);
                return val;
        }
        else
        if(eq(proc,recSymbol("parseCsv"))){
        	int i=0;
        	int j=0;
        	try{
        	String page=print.print(car(argl));
        	String tableArray=print.print(second(argl));
        	String rowArray=print.print(third(argl));
        	String colArray=print.print(fourth(argl));
            parseCsv(page,tableArray,rowArray,colArray);
        	}
        	catch(Exception e){
        		return nilSymbol;
        	}
        	return recSymbol("t");
        } // end if parseCsv
        else
        if(eq(proc,recSymbol("sumif"))||eq(proc,recSymbol("sumIf"))){
        	String table=print.print(car(argl));
        	String rowcol=print.print(second(argl));
        	int ix=((MyInt)(third(argl))).getInt();
        	LispObject operatoro=fourth(argl);
        	String operator=print.print(operatoro);
        	LispObject operand2=fourth(cdr(argl));
        	int sx=((MyInt)fourth(cdr(cdr(argl)))).getInt();
            return sumif(table,rowcol, ix, operator, operand2, sx);
        }
        else
        if(eq(proc,recSymbol("maxif"))||eq(proc,recSymbol("maxIf"))){
            String table=print.print(car(argl));
            String rowcol=print.print(second(argl));
            int ix=((MyInt)(third(argl))).getInt();
            LispObject operatoro=fourth(argl);
            String operator=print.print(operatoro);
            LispObject operand2=fourth(cdr(argl));
            int sx=((MyInt)fourth(cdr(cdr(argl)))).getInt();
            return maxif(table,rowcol, ix, operator, operand2, sx);
        }
        else
        if(eq(proc,recSymbol("minif"))||eq(proc,recSymbol("minIf"))){
            String table=print.print(car(argl));
            String rowcol=print.print(second(argl));
            int ix=((MyInt)(third(argl))).getInt();
            LispObject operatoro=fourth(argl);
            String operator=print.print(operatoro);
            LispObject operand2=fourth(cdr(argl));
            int sx=((MyInt)fourth(cdr(cdr(argl)))).getInt();
            return minif(table,rowcol, ix, operator, operand2, sx);
        }       
        else
        if(eq(proc,recSymbol("countif"))||eq(proc,recSymbol("countIf"))){
        	String table=print.print(car(argl));
        	String rowcol=print.print(second(argl));
        	int ix=((MyInt)(third(argl))).getInt();
        	LispObject operatoro=fourth(argl);
        	String operator=print.print(operatoro);
        	LispObject operand2=fourth(cdr(argl));
            return countif(table,rowcol, ix, operator, operand2);        	
        }
        else
        if(eq(proc,recSymbol("getindex"))||eq(proc,recSymbol("getIndex"))){
        	String table=print.print(car(argl));
        	String rowcol=print.print(second(argl));
        	int ix=((MyInt)(third(argl))).getInt();
        	LispObject operatoro=fourth(argl);
        	String operator=print.print(operatoro);
        	LispObject operand2=fourth(cdr(argl));
            return getIndex(table,rowcol, ix, operator, operand2);        	
        }        
        else
        if(eq(proc,recSymbol("getMaxIndex"))){
           	String rowcol=print.print(car(argl));
            return getMaxIndex(rowcol);        	
        }        
        else     	
        if(eq(proc,recSymbol("getResultPart"))){
        	String page=print.print(car(argl));
        	String rtn=getResultPart(page);
            MyString val=new MyString(rtn);
            return val;
        }
        else
        if(eq(proc,recSymbol("getBetween"))){
        	String page=print.print(car(argl));
        	String btag=print.print(second(argl));
        	String etag=print.print(third(argl));
        	String rtn=getBetween(page,btag,etag);
            MyString val=new MyString(rtn);
            return val;
        }
        else
        if(eq(proc,recSymbol("replaceAll"))){
        	String page=print.print(car(argl));
        	String s=print.print(second(argl));
        	String d=print.print(third(argl));
        	String rtn=replaceAll(page,s,d);
            MyString val=new MyString(rtn);
            return val;
        }
        else
        if(eq(proc,recSymbol("stoi"))){
        	try{
        	  String sv=print.print(car(argl));
        	  int v=(new Integer(sv)).intValue();
              MyInt val=new MyInt(v);
              return val;
        	}
        	catch(Exception e){
        		return nilSymbol;
        	}
        }
        else
        if(eq(proc,recSymbol("delay"))){
        	try{
        	  MyInt dx=(MyInt)(car(argl));
           	  int ix=(dx).getInt();
//        	  MyNumber dtime =(MyNumber)(car(argl));
        	  try{
//        		  Thread.sleep((long)(dtime.val));
        		  Thread.sleep((long)ix);
        	  }
        	  catch(Exception e){
        		  return nilSymbol;
        	  }
              return dx;
        	}
        	catch(Exception e){
        		return nilSymbol;
        	}
        } 
        else
        if(eq(proc,recSymbol("isDate"))){
      	    String sv=print.print(car(argl));
        	if(Util.isDate(sv)) return tSymbol;
        	return nilSymbol;
        }
        else
        if(eq(proc,recSymbol("date2l"))){
      	    String sv=print.print(car(argl));
        	if(!(Util.isDate(sv))) return nilSymbol;
        	long dx=Util.date2l(sv);
            MyLong val=new MyLong(dx);
            return val;        	
        }
        else
        if(eq(proc,recSymbol("l2date"))){
      	    MyLong dx=(MyLong)(car(argl));
       	    Long ix=(dx).getLong();
        	String sd=Util.l2date(ix);
            MyString val=new MyString(sd);
            return val;
        }
        else
        if(eq(proc,recSymbol("tokenize"))){
        	// tokenize(string,breakSymbols, array)
        	try{
               String str=print.print(car(argl));
               String bks=print.print(second(argl));
               String tbl=print.print(third(argl));
      	       StringTokenizer st=new StringTokenizer(str,bks);
      	       int i=0;
      	       while(st.hasMoreElements()){
       	          String xx=st.nextToken();
                  MyString val=new MyString(xx);
			      LispObject index=cons(new MyInt(i),nilSymbol);
			      String sindex=print.print(index);
                  arrays.put(tbl,sindex,val);
                  i++;
			   }
               MyInt val=new MyInt(i);
               return val;        	
        	}
        	catch(Exception e){
        		return nilSymbol;
        	}
        }
        return null;
    }
    
    public String getBetween(String page, String beginTag, String endTag){
    	return Util.getBetween(page, beginTag, endTag);
    }
    public String replaceAll(String x, String s, String d){
    	return x.replaceAll(s, d);
    }
    public String getResultPart(String input){
//		String input=Util.getBetween(page,"<pre>","</pre>");
		if(input==null) return "";
		StringTokenizer st=new StringTokenizer(input,"\n");
		String rtn="";
		while(st.hasMoreTokens()){
			String line=st.nextToken();
			if(line.startsWith("result:")){
				break;
			}
		}
		while(st.hasMoreTokens()){
			String line=st.nextToken();
			if(line.startsWith("control:")){
				break;
			}		
			if(line.startsWith("error:")){
				break;
			}			
			rtn=rtn+line+"\n";			
		}
        rtn=rtn.replaceAll("&quot;", "\"");
        rtn=rtn.replaceAll("&lt;", "<");
        rtn=rtn.replaceAll("&gt;", ">");
    	return rtn;
    }
    public void parseCsv(String page, String table, String rowLabel, String colLabel){
    	int i=0;
    	int j=0;
    	int jmax=0;
    	Vector<String> vcol=new Vector();
    	int vcolMax=0;
    	try{
   	    StringTokenizer st=new StringTokenizer(page,"\n");
    	while(st.hasMoreTokens()){
    		long ct0=System.currentTimeMillis();
        	j=0;
			LispObject ix=cons(new MyInt(j),nilSymbol);
			String ixx=print.print(ix);
    		String line=st.nextToken();
    		if(i%10==0)
    		   printMessage("csvparsing "+i+":"+line);
    		StringTokenizer tx=new StringTokenizer(line,",");
        	while(tx.hasMoreTokens()){
        		long ct1=System.currentTimeMillis();
				LispObject jx=cons(new MyInt(j),nilSymbol);
				String jxx=print.print(jx);
    			String element=tx.nextToken();
    			element=Util.skipSpace(element);
    			element=Util.deleteLastSpace(element);
    			if(element.endsWith(".")){
    				element=element.substring(0,element.length()-1);
    			}
    			String[] strc=new String[1];
    			String[] rest=new String[1];
    			String[] left=new String[1];
    			String[] right=new String[1];
    			int[] intrtn=new int[1];
    			double[] drtn=new double[1];
    			LispObject index=cons(new MyInt(i),cons(new MyInt(j),nilSymbol));
    			String sindex=print.print(index);
    			if(Util.parseStrConst(element,strc,rest)){
//                    arrays.put(table,sindex,new MyString("\""+strc[0]+"\""));
    				arrays.put(table, sindex,new MyString(strc[0]));
    			}
    			else
    			if(Util.parseDouble(element, drtn, rest)){
    				arrays.put(table, sindex, new MyDouble(drtn[0]));
    			}
    			else
    			if(Util.parseInt(element,intrtn,rest)){
    				arrays.put(table, sindex, new MyInt(intrtn[0]));
    			}
    			else
    			if(Util.parseEquation(element,left,right)){
    				LispObject v=nilSymbol;
    				if(Util.isDate(right[0])){
    					v=new MyString(right[0]);
    				}
    				else
    				if(Util.parseHex(right[0],  strc, rest)){
    					v=new MyString(strc[0]);    					
    				}
    				else
    				if(Util.parseDouble(right[0], drtn, rest)){
    					v=new MyDouble(drtn[0]);
    				}
    				else
    				if(Util.parseInt(right[0], intrtn, rest)){
    					v=new MyInt(intrtn[0]);
    				}
    				else
    				if(Util.parseStrConst(right[0], strc, rest)){
//    					v=new MyString("\""+strc[0]+"\"");
    					v=new MyString(strc[0]);
    				}
    				else{
    					v=new MyString(right[0]);        					
    				}
//    				vcol.setElementAt(left[0], vcolMax);
//            		long ct2=System.currentTimeMillis();
//        			Log.d(TAG,"("+i+","+j+") ct1-ct0="+(ct1-ct0));
    				boolean found=false;
    				for(int k=0;k<vcol.size();k++){
    					if((vcol.elementAt(k)).equals(left[0])){
    						found=true;
    						if(k!=j){
			       			    index=cons(new MyInt(i),cons(new MyInt(k),nilSymbol));        							
			       			    sindex=print.print(index);
    						}
                            break;
    					}
    				} // end for k
    				if(!found){
    					vcol.addElement(left[0]);
	       			    String lindex=print.print(cons(new MyString(left[0]),nilSymbol));
	       			    if(gui.isTracing()){
	    				    printMessage("put(colLabel, "+lindex+","+(vcol.size()-1)+")");
	       			    }
	       			    arrays.put(colLabel, lindex, new MyInt(vcol.size()-1));
	       			    sindex=print.print(cons(new MyInt(i),cons(new MyInt(vcol.size()-1),nilSymbol)));
    				}
//    				printMessage("put(table, "+sindex+","+print.print(v)+")");
	       			arrays.put(table, sindex, v);
//	        		long ct3=System.currentTimeMillis();
//	    			Log.d(TAG,"("+i+","+j+") ct1-ct0="+(ct1-ct0)+" ct2-ct1="+(ct2-ct1)+" ct3-ct2="+(ct3-ct2));
        		} // end parseEquation
    			j++;
    		} // end while tx
    		i++;
    	} // end while st
    	}
    	catch(Exception e){
    		printMessage("parseCsv error row="+i+",col="+j+" "+e.toString());
    	}
    	try{
		   LispObject rindex=cons(new MyString("rowcol"),nilSymbol); 
		   String srindex=print.print(rindex);
		   arrays.put(rowLabel, srindex, new MyString("row"));
		   LispObject rmax=cons(new MyString("maxIndex"),nilSymbol); 
		   String srmax=print.print(rmax);
		   arrays.put(rowLabel, srmax, new MyInt(i));
		   arrays.put(colLabel, srindex, new MyString("col"));
		   arrays.put(colLabel, srmax, new MyInt(vcol.size()-1));    	
    	}
    	catch(Exception e){
    		printMessage("parseCsv Error.."+e.toString());
    	}
    }
    
    LispObject sumif(String table, String rowcol, 
    		         int operand1, String operator, LispObject operand2, int operand3){

    	LispObject rindex=cons(new MyString("rowcol"),nilSymbol); 
		String srindex=print.print(rindex);
    	String rc=print.print(arrays.get(rowcol, srindex));
    	if(rc.equals("row")){
			rindex=cons(new MyString("maxIndex"),nilSymbol); 
			srindex=print.print(rindex);
			LispObject ix=arrays.get(rowcol, srindex);
			if(ix==null)return nilSymbol;
    		int maxindex=((MyInt)(ix)).getInt();
    		double ssx=0.0;
    		for(int i=0;i<maxindex;i++){
    			LispObject index=cons(new MyInt(i),cons(new MyInt(operand1),nilSymbol));
    			String sindex=print.print(index);
    			LispObject v=arrays.get(table, sindex);
    			if(v==null)continue;
    			if(apply(recSymbol(operator),
    					cons(v,cons(operand2,nilSymbol)),nilSymbol)!=nilSymbol){
    				LispObject index2=cons(new MyInt(i),cons(new MyInt(operand3),nilSymbol));
    				String sindex2=print.print(index2);
    				LispObject v2=arrays.get(table, sindex2);
        			double w=0;
        			if(v2.isKind("mydouble")){
        				w=((MyDouble)v2).val;
        			}
        			else
        			if(v2.isKind("myint")){
        				w=(double)(((MyInt)v2).val);
        			}
        			ssx=ssx+w;
    			}
    		}
    		int ssx2=(int)(Math.round(ssx));
    		if(Math.abs(ssx-ssx2)<0.00001){
    			return new MyInt(ssx2);
    		}
    		return new MyDouble(ssx);
    	}
    	else{
			rindex=cons(new MyString("maxIndex"),nilSymbol); 
			srindex=print.print(rindex);
			LispObject ix=arrays.get(rowcol, srindex);
			if(ix==null) return nilSymbol;
    		int maxindex=((MyInt)(ix)).getInt();
    		double ssx=0.0;
    		for(int j=0;j<maxindex;j++){
    			LispObject index=cons(new MyInt(operand1),cons(new MyInt(j),nilSymbol));
    			String sindex=print.print(index);
    			LispObject v=arrays.get(table, sindex);
    			if(v==null) continue;
    			if(apply(recSymbol(operator),
    					cons(v,cons(operand2,nilSymbol)),nilSymbol)!=nilSymbol){
    				LispObject index2=cons(new MyInt(operand3),cons(new MyInt(j),nilSymbol));
    				String sindex2=print.print(index2);
    				LispObject v2=arrays.get(table, sindex2);
        			double w=0;
        			if(v.isKind("mydouble")){
        				w=((MyDouble)v).val;
        			}
        			else
        			if(v.isKind("myint")){
        				w=(double)(((MyInt)v).val);
        			}
        			ssx=ssx+w;
    			}
    		}
    		int ssx2=(int)(Math.round(ssx));
    		if(Math.abs(ssx-ssx2)<0.00001){
    			return new MyInt(ssx2);
    		}
    		return new MyDouble(ssx);	
    	}
    }
    
    LispObject maxif(String table, String rowcol, 
	         int operand1, String operator, LispObject operand2, int operand3){

         LispObject rindex=cons(new MyString("rowcol"),nilSymbol); 
         String srindex=print.print(rindex);
         String rc=print.print(arrays.get(rowcol, srindex));
         if(rc.equals("row")){
	        rindex=cons(new MyString("maxIndex"),nilSymbol); 
	        srindex=print.print(rindex);
	        int maxindex=((MyInt)(arrays.get(rowcol, srindex))).getInt();
	        boolean firstFlag=true;
	        double ssx=0.0;
	        for(int i=0;i<maxindex;i++){
		       LispObject index=cons(new MyInt(i),cons(new MyInt(operand1),nilSymbol));
		       String sindex=print.print(index);
		       LispObject v=arrays.get(table, sindex);
		       if(v==null) continue;
		       if(apply(recSymbol(operator),
				         cons(v,cons(operand2,nilSymbol)),nilSymbol)!=nilSymbol){
			       LispObject index2=cons(new MyInt(i),cons(new MyInt(operand3),nilSymbol));
			       String sindex2=print.print(index2);
			       LispObject v2=arrays.get(table, sindex2);
			       double w=0;
			       if(v2.isKind("mydouble")){
			       	   w=((MyDouble)v2).val;
			       }
			       else
			       if(v2.isKind("myint")){
				       w=(double)(((MyInt)v2).val);
			       }
			       if(firstFlag)
			          ssx=w;
			       else{
			    	   if(ssx<w) ssx=w;
			       }
		       }
	        }
	        int ssx2=(int)(Math.round(ssx));
	        if(Math.abs(ssx-ssx2)<0.00001){
		        return new MyInt(ssx2);
	        }
	        return new MyDouble(ssx);
         }
         else{
	        rindex=cons(new MyString("maxIndex"),nilSymbol); 
	        srindex=print.print(rindex);
	        int maxindex=((MyInt)(arrays.get(rowcol, srindex))).getInt();
	        double ssx=0.0;
	        boolean firstFlag=true;
	        for(int j=0;j<maxindex;j++){
		        LispObject index=cons(new MyInt(operand1),cons(new MyInt(j),nilSymbol));
		        String sindex=print.print(index);
		        LispObject v=arrays.get(table, sindex);
		        if(apply(recSymbol(operator),
				    cons(v,cons(operand2,nilSymbol)),nilSymbol)!=nilSymbol){
			    LispObject index2=cons(new MyInt(operand3),cons(new MyInt(j),nilSymbol));
			    String sindex2=print.print(index2);
			    LispObject v2=arrays.get(table, sindex2);
			    double w=0;
			    if(v.isKind("mydouble")){
				    w=((MyDouble)v).val;
			    }
			    else
			    if(v.isKind("myint")){
				    w=(double)(((MyInt)v).val);
			    }
			    if(firstFlag)
				    ssx=w;
				else{
				    if(ssx<w) ssx=w;
				}
		    }
	     }
	     int ssx2=(int)(Math.round(ssx));
	     if(Math.abs(ssx-ssx2)<0.00001){
		     return new MyInt(ssx2);
	     }
	     return new MyDouble(ssx);	
       }
    }   
    
    LispObject minif(String table, String rowcol, 
	         int operand1, String operator, LispObject operand2, int operand3){

        LispObject rindex=cons(new MyString("rowcol"),nilSymbol); 
        String srindex=print.print(rindex);
        String rc=print.print(arrays.get(rowcol, srindex));
        if(rc.equals("row")){
	        rindex=cons(new MyString("maxIndex"),nilSymbol); 
	        srindex=print.print(rindex);
	        int maxindex=((MyInt)(arrays.get(rowcol, srindex))).getInt();
	        boolean firstFlag=true;
	        double ssx=0.0;
	        for(int i=0;i<maxindex;i++){
		       LispObject index=cons(new MyInt(i),cons(new MyInt(operand1),nilSymbol));
		       String sindex=print.print(index);
		       LispObject v=arrays.get(table, sindex);
		       if(apply(recSymbol(operator),
				         cons(v,cons(operand2,nilSymbol)),nilSymbol)!=nilSymbol){
			       LispObject index2=cons(new MyInt(i),cons(new MyInt(operand3),nilSymbol));
			       String sindex2=print.print(index2);
			       LispObject v2=arrays.get(table, sindex2);
			       double w=0;
			       if(v2.isKind("mydouble")){
			       	   w=((MyDouble)v2).val;
			       }
			       else
			       if(v2.isKind("myint")){
				       w=(double)(((MyInt)v2).val);
			       }
			       if(firstFlag)
			          ssx=w;
			       else{
			    	   if(ssx>w) ssx=w;
			       }
		       }
	        }
	        int ssx2=(int)(Math.round(ssx));
	        if(Math.abs(ssx-ssx2)<0.00001){
		        return new MyInt(ssx2);
	        }
	        return new MyDouble(ssx);
        }
        else{
	        rindex=cons(new MyString("maxIndex"),nilSymbol); 
	        srindex=print.print(rindex);
	        int maxindex=((MyInt)(arrays.get(rowcol, srindex))).getInt();
	        double ssx=0.0;
	        boolean firstFlag=true;
	        for(int j=0;j<maxindex;j++){
		        LispObject index=cons(new MyInt(operand1),cons(new MyInt(j),nilSymbol));
		        String sindex=print.print(index);
		        LispObject v=arrays.get(table, sindex);
		        if(apply(recSymbol(operator),
				    cons(v,cons(operand2,nilSymbol)),nilSymbol)!=nilSymbol){
			    LispObject index2=cons(new MyInt(operand3),cons(new MyInt(j),nilSymbol));
			    String sindex2=print.print(index2);
			    LispObject v2=arrays.get(table, sindex2);
			    double w=0;
			    if(v.isKind("mydouble")){
				    w=((MyDouble)v).val;
			    }
			    else
			    if(v.isKind("myint")){
				    w=(double)(((MyInt)v).val);
			    }
			    if(firstFlag)
				    ssx=w;
				else{
				    if(ssx>w) ssx=w;
				}
		    }
	     }
	     int ssx2=(int)(Math.round(ssx));
	     if(Math.abs(ssx-ssx2)<0.00001){
		     return new MyInt(ssx2);
	     }
	     return new MyDouble(ssx);	
       }
    }       
    
    LispObject getMaxIndex(String rowcol ){
        LispObject rindex=cons(new MyString("maxIndex"),nilSymbol); 
        String srindex=print.print(rindex);
        LispObject v=arrays.get(rowcol, srindex);
        if(v==null) return nilSymbol;
        return v;
    }
    
    LispObject countif(String table, String rowcol, 
	         int operand1, String operator, LispObject operand2){

        LispObject rindex=cons(new MyString("rowcol"),nilSymbol); 
        String srindex=print.print(rindex);
        LispObject ix=arrays.get(rowcol,srindex);        
        if(ix==null) return nilSymbol;
        String rc=print.print(ix);
        if(rc.equals("row")){
	        rindex=cons(new MyString("maxIndex"),nilSymbol); 
	        srindex=print.print(rindex);
	        LispObject v=arrays.get(rowcol, srindex);
	        if(v==null) return nilSymbol;
	        int maxindex=((MyInt)(v)).getInt();
	        int ssx=0;
	        for(int i=0;i<maxindex;i++){
		        LispObject index=cons(new MyInt(i),cons(new MyInt(operand1),nilSymbol));
		        String sindex=print.print(index);
		        v=arrays.get(table, sindex);
		        if(v==null) continue;
		        if(apply(recSymbol(operator),
				    cons(v,cons(operand2,nilSymbol)),nilSymbol)!=nilSymbol){
			        ssx++;
		        }
	        }
		    return new MyInt(ssx);
        }
        else{
	        rindex=cons(new MyString("maxIndex"),nilSymbol); 
	        srindex=print.print(rindex);
	        int maxindex=((MyInt)(arrays.get(rowcol, srindex))).getInt();
	        int ssx=0;
	        for(int j=0;j<maxindex;j++){
		        LispObject index=cons(new MyInt(operand1),cons(new MyInt(j),nilSymbol));
		        String sindex=print.print(index);
		        LispObject v=arrays.get(table, sindex);
		        if(v==null) continue;
		        if(apply(recSymbol(operator),
				    cons(v,cons(operand2,nilSymbol)),nilSymbol)!=nilSymbol){
			        ssx++;
		        }
	        }
		    return new MyInt(ssx);
        }
    }    
    /*
     * in the table[i][j],
     * if rowcol="row",
     *    find out the index i, which satisfy
     *         table[i][operand1] and operator(table[i][operand1],operand2) is true,
     * else
     *    find out the index j, which satisfy
     *         table[operand1][j] and operator(table[operand1][j],operand2) is true.
     *         
     * for example,
     *   if table[3][4]="name1" and there is no other "name1" in the table[*][4],
     *   getIndex(table,"row", 4, "=", "name") returns 3.
     */
    LispObject getIndex(String table, String rowcol, 
	         int operand1, String operator, LispObject operand2){
    	LispObject rindex=cons(new MyString("rowcol"),nilSymbol); 
    	String srindex=print.print(rindex);
    	LispObject ix=arrays.get(rowcol, srindex);
    	if(ix==null)return nilSymbol;
    	String rc=print.print(ix);
    	if(rc.equals("row")){
    		rindex=cons(new MyString("maxIndex"),nilSymbol); 
    		srindex=print.print(rindex);
    		ix=arrays.get(rowcol, srindex);
    		if(ix==null) return nilSymbol;
    		int maxindex=((MyInt)(ix)).getInt();
    		for(int i=0;i<maxindex;i++){
    			LispObject index=cons(new MyInt(i),cons(new MyInt(operand1),nilSymbol));
    			String sindex=print.print(index);
    			LispObject v=arrays.get(table, sindex);
    			if(v==null) continue;
    			if(apply(recSymbol(operator),
    					cons(v,cons(operand2,nilSymbol)),nilSymbol)!=nilSymbol){
    	   			return new MyInt(i);   
    			}
    		}
    		return nilSymbol;
    	}
    	else{
    		rindex=cons(new MyString("maxIndex"),nilSymbol); 
    		srindex=print.print(rindex);
    		ix=arrays.get(rowcol, srindex);
    		if(ix==null)return nilSymbol;
    		int maxindex=((MyInt)(ix)).getInt();
    		for(int j=0;j<maxindex;j++){
    			LispObject index=cons(new MyInt(operand1),cons(new MyInt(j),nilSymbol));
    			String sindex=print.print(index);
    			LispObject v=arrays.get(table, sindex);
    			if(apply(recSymbol(operator),
    					cons(v,cons(operand2,nilSymbol)),nilSymbol)!=nilSymbol){
    	   			return new MyInt(j); 	
    			}
    		}
    		return nilSymbol;	
    	}
    }
   
    
    InterpreterInterface service;
    public Basic(String inputArea, CQueue q, InterpreterInterface bf)
   {
       super(inputArea, q, bf);
       service=bf;
       this.waitingTable=new Hashtable();
   }    
    
    int maxwait=6000;
	public String waitForResult(String x)
	{   
		System.out.println("...waiting "+x);
		for(int i=0;i<maxwait;i++){
			synchronized(waitingTable){
			    String rtn=(String)(this.waitingTable.get(x));
			    System.out.println(" ... return "+rtn);
			    if(rtn!=null) {
				   this.waitingTable.remove(x);
				   return rtn;
			    }
			}
			try{
				Thread.sleep(10);
			}
			catch(Exception e){}
		}
		System.out.println("time out");
		return "time-out";
	}
//	@Override
	public String getOutputText() {
		// TODO Auto-generated method stub
		return service.getOutputText();
	}
//	@Override
	public boolean isTracing() {
		// TODO Auto-generated method stub
		return service.isTracing();
	}
	String inputProgram="";
//	@Override
	public String parseCommand(String x) {
		// TODO Auto-generated method stub
		String[] rest=new String[1];
		if(Util.parseKeyWord(x,"run ",rest)){
			if(!this.running){
				this.running=true;
			    this.runProgram(inputProgram);
			    this.running=false;
			    return "OK";
			}
			else{
				return "ERROR already running.";
			}
		}
		else
		if(Util.parseKeyWord(x,"setInput ",rest)){
			this.inputProgram=rest[0];
			return "OK";
		}
		else
		if(Util.parseKeyWord(x,"eval ",rest)){
			String input=rest[0];
			inqueue.putString(input);
			LispObject o=((ReadLine)(read)).readProgram(inqueue);
	        LispObject p=this.basicparser.parseBasic(o);
			this.eval(p,this.environment);
			return "OK";
		}
		return null;
	}
//	@Override
	public InterpreterInterface lookUp(String x) {
		// TODO Auto-generated method stub
		return service.lookUp(x);
	}

}