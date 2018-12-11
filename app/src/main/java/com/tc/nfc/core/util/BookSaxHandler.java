package com.tc.nfc.core.util;

import com.tc.nfc.model.Book;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;

/**
 * Created by tangjiarao on 16/12/21.
 */
public class BookSaxHandler extends DefaultHandler {

    //声明一个装载Beauty类型的List
    private List<Book> mList;
    //声明一个Beauty类型的变量
    private Book book;
    //声明一个字符串变量
    private String content;

    /**
     * MySaxHandler的构造方法
     *
     * @param list 装载返回结果的List对象
     */
    public BookSaxHandler(List<Book> list){
        this.mList = list;
    }

    /**
     * 当SAX解析器解析到XML文档开始时，会调用的方法
     */
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    /**
     * 当SAX解析器解析到XML文档结束时，会调用的方法
     */
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    /**
     * 当SAX解析器解析到某个属性值时，会调用的方法
     * 其中参数ch记录了这个属性值的内容
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);
        content = new String(ch, start, length);
    }

    /**
     * 当SAX解析器解析到某个元素开始时，会调用的方法
     * 其中localName记录的是元素属性名
     */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if("return".equals(localName)){
            book = new Book(); //新建Beauty对象
        }
    }

    /**
     * 当SAX解析器解析到某个元素结束时，会调用的方法
     * 其中localName记录的是元素属性名
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        super.endElement(uri, localName, qName);

        if("author".equals(localName)){
            book.setAuthor(content);
        }else if("bookrecno".equals(localName)){
            book.setBookrecno(content);
        }else if("booktype".equals(localName)){
            book.setBooktype(content);
        }else if("classNo".equals(localName)){
            book.setClassNo(content);
        }else if("isbn".equals(localName)){
            book.setIsbn(content.replace("-", ""));
        }else if("publisher".equals(localName)){
            book.setPublisher(content);
        }else if("title".equals(localName)){
            book.setBookTitle(content);
        }else if("barcode".equals(localName)){
            book.setBookBarcode(content);
        }else if("callno".equals(localName)){
            book.setCallno(content);
        }else if("loanCount".equals(localName)){
            book.setLoanCount(content);
        }else if("loanDateInStr".equals(localName)){
            book.setLoanDate(content);
        }else if("returnDateInStr".equals(localName)){
            book.setReturnDate(content);
        }else if("return".equals(localName)){
            book.setIsCheck(false);
            mList.add(book); //将Beauty对象加入到List中
        }
    }
}