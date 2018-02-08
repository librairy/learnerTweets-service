package org.librairy.service.learner.service.btm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class Corpus {

    List<Document> documents;


    public Corpus(){
        documents = new ArrayList<>();
    }

    public Corpus add(Document document){
        documents.add(document);
        return this;
    }

    public Integer getSize(){
        return documents.size();
    }

    public Document get(Integer index){
        return documents.get(index);
    }

    public List<Document> getDocuments(){
        return this.documents;
    }
}
