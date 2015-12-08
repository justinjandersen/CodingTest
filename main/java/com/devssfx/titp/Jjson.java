package com.devssfx.titp;

import java.util.ArrayList;

public class Jjson {

    public class NameValues {
        public String Name;
        public String Value;
    }

    public ArrayList<NameValues> nameValueList;

    public Jjson(String jsonString){

        nameValueList = new ArrayList<NameValues>();

        //if(jsonString.startsWith("[") && jsonString.endsWith("]")) {
        //    jsonString = jsonString.substring(1, jsonString.length() - 2);

            int iStart;
            int iEnd;
            boolean inName;
            NameValues item;
            boolean inString;

            inName = true;
            inString = false;
            item = new NameValues();

            iEnd = 0;
            do {
                iStart = jsonString.indexOf("\"", iEnd);
                inString = true;
                if(!inName) {
                    int iComma = jsonString.indexOf(",", iEnd);
                    if (iStart == -1 || (iComma != -1 && iComma < iStart)) {
                        iStart = jsonString.indexOf(":", iEnd); //comma before quote, so maybe number
                        inString = false;
                    }
                }
                if(iStart != -1) {
                    if(inString) {
                        do { //find end quote if in string
                            iEnd = jsonString.indexOf("\"", iStart + 1);
                        } while (iEnd > -1 && jsonString.substring(iEnd - 1, iEnd).equals("\\"));
                        if(iEnd == -1) iEnd = jsonString.length();
                    }else{
                        iEnd = jsonString.indexOf(",", iStart+1);
                        int iBracket = jsonString.indexOf("}", iStart+1);
                        if(iEnd == -1 || (iBracket != -1 && iBracket < iEnd))
                            iEnd = iBracket;

                        if(iEnd == -1) {
                            //iEnd = jsonString.indexOf("}", iStart+1);
                            //if(iEnd == -1) {
                                iEnd = jsonString.indexOf("]", iStart+1);
                                if(iEnd == -1) {
                                    iEnd = jsonString.length();
                                }
                            //}
                        }
                    }

                    if (inName) {
                        item.Name = jsonString.substring(iStart + 1, iEnd).trim();
                        iEnd++;
                    } else {
                        item.Value = jsonString.substring(iStart + 1, iEnd).trim();
                        nameValueList.add(item);
                        item = new NameValues();
                        iEnd = jsonString.indexOf("\"",iEnd + 1);
                    }
                    inName = !inName;
                }
            }while(iStart != -1 && iEnd != -1);

        //}

    }

    public String getValue(String name){
        String rtn = "";

        for(NameValues item : nameValueList){
            if(name.equals(item.Name)){
                rtn = item.Value;
                rtn = rtn.replace("\\u0027","'");
                break;
            }
        }

        return rtn;
    }

//    private JItem jBase;
//    public Jjson(String jsonString){
//
//        jBase = new JItem();
//        JItem jItem, jItemNew;
//
//        int i1, i2; //, iQuote, iComma, iColon, iCurlyOpen, iCurlyClose;
//        Boolean inQuote;
//        Boolean inName;
//
//        if(jsonString.startsWith("[") && jsonString.endsWith("]")){
//            jsonString = jsonString.substring(1, jsonString.length()-2);
//
//            i1=0;
////            iQuote = jsonString.indexOf("\"",i);
////            iComma = jsonString.indexOf(",",i);
////            iColon = jsonString.indexOf(":",i);
////            iCurlyOpen = jsonString.indexOf("{",i);
////            iCurlyClose = jsonString.indexOf("}",i);
//
//            i2 = i1;
//            inQuote = false;
//            inName = false;
//            jItem = null;
//            jItemNew = null;
//
//            switch (jsonString.substring(i2, i2+1)){
//                case "{":
//                    jItemNew = new JItem();
//                    if(jItem == null) {
//                        jBase.AddObj(jItemNew);
//                        jItemNew.Parent = jBase;
//                    }else{
//                        jItem.AddObj(jItemNew);
//                        jItemNew.Parent = jItem;
//                    }
//                    jItem = jItemNew;
//                    break;
//                case "}":
//                    break;
//                case ",":
//                    inName = true;
//                    break;
//                case ":" :
//                    if(inName){
//                        inName = false;
//                    }else{
//                        inName = true;
//                    }
//                    break;
//                case "\"":
//                    if(inQuote){
//                        inQuote = false;
//                    }else{
//                        inQuote = true;
//                    }
//                    break;
//                default:
//
//                    break;
//            }
//
//
//
//
//        }
//
//    }
//
//    public class JItem{
//
//        public String Name;
//        public Object Value;
//        public int ValType; //0=null, 1=string, 2=int, 3=boolean, 4=list
//        public JItem Parent;
//
//        public JItem(){
//            Value = null;
//            ValType = 0;
//            Parent = null;
//        }
//
//        public void AddObj(JItem item){
//            Value = item;
//            ValType = 4;
//        }
//    }

}
