package org.librairy.service.learner.service.btm;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
public class BiTerm {

    private static final Integer MASK = 1000000;

    Long id;

    public BiTerm(Integer w1,Integer w2){
        id = (long)w1*MASK+w2;
    }

    public Long getId() {
        return id;
    }

    public int getWord1(){
        return (int) (id / MASK);
    }

    public int getWord2(){
        return (int) (id % MASK);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BiTerm biTerm = (BiTerm) o;

        return id.equals(biTerm.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
