package org.intermine.objectstore.query.iql;

/*
 * Copyright (C) 2002-2005 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import junit.framework.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.intermine.testing.OneTimeTestCase;
import org.intermine.objectstore.SetupDataTestCase;

/**
 * Test case that sets up various IqlQueries.
 * This class extends SetupDataTestCase because we use various example
 * objects as part of the tests. These are set up in
 * SetupDataTestCase. We do not actually contact the database in this
 * test.
 *
 * @author Matthew Wakeling
 * @author Andrew Varley
 */
public abstract class IqlQueryTestCase extends SetupDataTestCase
{
    public IqlQueryTestCase(String arg) {
        super(arg);
    }

    public static Test suite() {
        return OneTimeTestCase.buildSuite(IqlQueryTestCase.class);
    }

    public static void oneTimeSetUp() throws Exception {
        SetupDataTestCase.oneTimeSetUp();

        setUpResults();
    }

    /**
     * Set up all the results expected for a given subset of queries
     */
    public static void setUpResults() {
        results.put("SelectSimpleObject", new IqlQuery("SELECT Alias FROM org.intermine.model.testmodel.Company AS Alias", null));
        results.put("SubQuery", new IqlQuery("SELECT DISTINCT \"All\".Array.name AS a1_, \"All\".Alias AS Alias FROM (SELECT DISTINCT Array, 5 AS Alias FROM org.intermine.model.testmodel.Company AS Array) AS \"All\"", null));
        results.put("WhereSimpleEquals", new IqlQuery("SELECT DISTINCT a1_.name AS a2_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE a1_.vatNumber = 1234", null));
        results.put("WhereSimpleNotEquals", new IqlQuery("SELECT DISTINCT a1_.name AS a2_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE a1_.vatNumber != 1234", null));
        results.put("WhereSimpleLike", new IqlQuery("SELECT DISTINCT a1_.name AS a2_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE a1_.name LIKE 'Company%'", null));
        results.put("WhereEqualsString", new IqlQuery("SELECT DISTINCT a1_.name AS a2_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE a1_.name = 'CompanyA'", null));
        results.put("WhereAndSet", new IqlQuery("SELECT DISTINCT a1_.name AS a2_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE (a1_.name LIKE 'Company%' AND a1_.vatNumber > 2000)", null));
        results.put("WhereOrSet", new IqlQuery("SELECT DISTINCT a1_.name AS a2_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE (a1_.name LIKE 'CompanyA%' OR a1_.vatNumber > 2000)", null));
        results.put("WhereNotSet", new IqlQuery("SELECT DISTINCT a1_.name AS a2_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE ( NOT (a1_.name LIKE 'Company%' AND a1_.vatNumber > 2000))", null));
        results.put("WhereSubQueryField", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Department AS a1_ WHERE a1_.name IN (SELECT DISTINCT a1_.name AS a2_ FROM org.intermine.model.testmodel.Department AS a1_) ORDER BY a1_.name", null));
        results.put("WhereSubQueryClass", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE a1_ IN (SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE a1_.name = 'CompanyA')", null));
        results.put("WhereNotSubQueryClass", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE a1_ NOT IN (SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE a1_.name = 'CompanyA')", null));
        results.put("WhereClassClass", new IqlQuery("SELECT DISTINCT a1_, a2_ FROM org.intermine.model.testmodel.Company AS a1_, org.intermine.model.testmodel.Company AS a2_ WHERE a1_ = a2_", null));
        IqlQuery fq = new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE a1_ = ?", null);
        fq.setParameters(Collections.singletonList(data.get("CompanyA")));
        results.put("WhereClassObject", fq);
        results.put("WhereNotClassClass", new IqlQuery("SELECT DISTINCT a1_, a2_ FROM org.intermine.model.testmodel.Company AS a1_, org.intermine.model.testmodel.Company AS a2_ WHERE a1_ != a2_", null));
        results.put("Contains11", new IqlQuery("SELECT DISTINCT a1_, a2_ FROM org.intermine.model.testmodel.Department AS a1_, org.intermine.model.testmodel.Manager AS a2_ WHERE (a1_.manager CONTAINS a2_ AND a1_.name = 'DepartmentA1')", null));
        results.put("ContainsNot11", new IqlQuery("SELECT DISTINCT a1_, a2_ FROM org.intermine.model.testmodel.Department AS a1_, org.intermine.model.testmodel.Manager AS a2_ WHERE (a1_.manager DOES NOT CONTAIN a2_ AND a1_.name = 'DepartmentA1')", null));
        results.put("Contains1N", new IqlQuery("SELECT DISTINCT a1_, a2_ FROM org.intermine.model.testmodel.Company AS a1_, org.intermine.model.testmodel.Department AS a2_ WHERE (a1_.departments CONTAINS a2_ AND a1_.name = 'CompanyA')", null));
        results.put("ContainsN1", new IqlQuery("SELECT DISTINCT a1_, a2_ FROM org.intermine.model.testmodel.Department AS a1_, org.intermine.model.testmodel.Company AS a2_ WHERE (a1_.company CONTAINS a2_ AND a2_.name = 'CompanyA')", null));
        results.put("ContainsMN", new IqlQuery("SELECT DISTINCT a1_, a2_ FROM org.intermine.model.testmodel.Contractor AS a1_, org.intermine.model.testmodel.Company AS a2_ WHERE (a1_.companys CONTAINS a2_ AND a1_.name = 'ContractorA')", null));
        results.put("ContainsDuplicatesMN", new IqlQuery("SELECT DISTINCT a1_, a2_ FROM org.intermine.model.testmodel.Contractor AS a1_, org.intermine.model.testmodel.Company AS a2_ WHERE a1_.oldComs CONTAINS a2_", null));
        fq = new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Department AS a1_ WHERE a1_.manager CONTAINS ?", null);
        fq.setParameters(Collections.singletonList(data.get("EmployeeA1")));
        results.put("ContainsObject", fq);
        results.put("SimpleGroupBy", new IqlQuery("SELECT DISTINCT a1_, COUNT(*) AS a2_ FROM org.intermine.model.testmodel.Company AS a1_, org.intermine.model.testmodel.Department AS a3_ WHERE a1_.departments CONTAINS a3_ GROUP BY a1_", null));
        results.put("MultiJoin", new IqlQuery("SELECT DISTINCT a1_, a2_, a3_, a4_ FROM org.intermine.model.testmodel.Company AS a1_, org.intermine.model.testmodel.Department AS a2_, org.intermine.model.testmodel.Manager AS a3_, org.intermine.model.testmodel.Address AS a4_ WHERE (a1_.departments CONTAINS a2_ AND a2_.manager CONTAINS a3_ AND a3_.address CONTAINS a4_ AND a3_.name = 'EmployeeA1')", null));
        results.put("SelectComplex", new IqlQuery("SELECT DISTINCT AVG(a1_.vatNumber) + 20 AS a3_, a2_.name AS a4_, a2_ FROM org.intermine.model.testmodel.Company AS a1_, org.intermine.model.testmodel.Department AS a2_ GROUP BY a2_", null));
        results.put("SelectClassAndSubClasses", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Employee AS a1_ ORDER BY a1_.name", null));
        results.put("SelectInterfaceAndSubClasses", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Employable AS a1_", null));
        results.put("SelectInterfaceAndSubClasses2", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.RandomInterface AS a1_", null));
        results.put("SelectInterfaceAndSubClasses3", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.ImportantPerson AS a1_", null));
        results.put("OrderByAnomaly", new IqlQuery("SELECT DISTINCT 5 AS a2_, a1_.name AS a3_ FROM org.intermine.model.testmodel.Company AS a1_", null));
        results.put("SelectUnidirectionalCollection", new IqlQuery("SELECT DISTINCT a2_ FROM org.intermine.model.testmodel.Company AS a1_, org.intermine.model.testmodel.Secretary AS a2_ WHERE (a1_.name = 'CompanyA' AND a1_.secretarys CONTAINS a2_)", null));
        fq = new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Company AS a1_, org.intermine.model.testmodel.Department AS a2_ WHERE (a1_ = ? AND a1_.departments CONTAINS a2_ AND a2_ IN (SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Department AS a1_ WHERE a1_ = ?))", null);
        fq.setParameters(Arrays.asList(new Object [] {data.get("CompanyA"), data.get("DepartmentA1")}));
        results.put("SelectClassObjectSubquery", fq);
        results.put("EmptyAndConstraintSet", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE true", null));
        results.put("EmptyOrConstraintSet", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Company AS a1_ WHERE false", null));
        Set bag1 = new HashSet();
        bag1.add("hello");
        bag1.add("goodbye");
        bag1.add("CompanyA");
        bag1.add(new Integer(5));
        fq = new IqlQuery("SELECT DISTINCT Company FROM org.intermine.model.testmodel.Company AS Company WHERE Company.name IN ?", null);
        fq.setParameters(Collections.singletonList(bag1));
        results.put("BagConstraint", fq);
        Set bag2 = new HashSet();
        bag2.add("hello");
        bag2.add("goodbye");
        bag2.add("CompanyA");
        bag2.add(new Integer(5));
        bag2.add(data.get("CompanyA"));
        fq = new IqlQuery("SELECT DISTINCT Company FROM org.intermine.model.testmodel.Company AS Company WHERE Company IN ?", null);
        fq.setParameters(Collections.singletonList(bag2));
        results.put("BagConstraint2", fq);
        results.put("InterfaceField", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Employable AS a1_ WHERE a1_.name = 'EmployeeA1'", null));
        fq = new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.HasAddress AS a1_, org.intermine.model.testmodel.Address AS a2_ WHERE (a1_.address CONTAINS a2_ AND a2_ = ?)", null);
        fq.setParameters(Collections.singletonList(data.get("Employee Street, AVille")));
        results.put("InterfaceReference", fq);
        fq = new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.HasSecretarys AS a1_, org.intermine.model.testmodel.Secretary AS a2_ WHERE (a1_.secretarys CONTAINS a2_ AND a2_ = ?)", null);
        fq.setParameters(Collections.singletonList(data.get("Secretary1")));
        results.put("InterfaceCollection", fq);
        Set res = new HashSet();
        res.add(new IqlQuery("SELECT DISTINCT a1_, a1_.debt AS a2_, a1_.age AS a3_ FROM (org.intermine.model.testmodel.Employee, org.intermine.model.testmodel.Broke) AS a1_ WHERE (a1_.debt > 0 AND a1_.age > 0)", null));
        res.add(new IqlQuery("SELECT DISTINCT a1_, a1_.debt AS a2_, a1_.age AS a3_ FROM (org.intermine.model.testmodel.Broke, org.intermine.model.testmodel.Employee) AS a1_ WHERE (a1_.debt > 0 AND a1_.age > 0)", null));
        results.put("DynamicInterfacesAttribute", res);
        res = new HashSet();
        res.add(new IqlQuery("SELECT DISTINCT a1_ FROM (org.intermine.model.testmodel.Employable, org.intermine.model.testmodel.Broke) AS a1_", null));
        res.add(new IqlQuery("SELECT DISTINCT a1_ FROM (org.intermine.model.testmodel.Broke, org.intermine.model.testmodel.Employable) AS a1_", null));
        results.put("DynamicClassInterface", res);
        res = new HashSet();
        res.add(new IqlQuery("SELECT DISTINCT a1_, a2_, a3_ FROM (org.intermine.model.testmodel.Department, org.intermine.model.testmodel.Broke) AS a1_, org.intermine.model.testmodel.Company AS a2_, org.intermine.model.testmodel.Bank AS a3_ WHERE (a2_.departments CONTAINS a1_ AND a3_.debtors CONTAINS a1_)", null));
        res.add(new IqlQuery("SELECT DISTINCT a1_, a2_, a3_ FROM (org.intermine.model.testmodel.Broke, org.intermine.model.testmodel.Department) AS a1_, org.intermine.model.testmodel.Company AS a2_, org.intermine.model.testmodel.Bank AS a3_ WHERE (a2_.departments CONTAINS a1_ AND a3_.debtors CONTAINS a1_)", null));
        results.put("DynamicClassRef1", res);
        res = new HashSet();
        res.add(new IqlQuery("SELECT DISTINCT a1_, a2_, a3_ FROM (org.intermine.model.testmodel.Department, org.intermine.model.testmodel.Broke) AS a1_, org.intermine.model.testmodel.Company AS a2_, org.intermine.model.testmodel.Bank AS a3_ WHERE (a1_.company CONTAINS a2_ AND a1_.bank CONTAINS a3_)", null));
        res.add(new IqlQuery("SELECT DISTINCT a1_, a2_, a3_ FROM (org.intermine.model.testmodel.Broke, org.intermine.model.testmodel.Department) AS a1_, org.intermine.model.testmodel.Company AS a2_, org.intermine.model.testmodel.Bank AS a3_ WHERE (a1_.company CONTAINS a2_ AND a1_.bank CONTAINS a3_)", null));
        results.put("DynamicClassRef2", res);
        res = new HashSet();
        res.add(new IqlQuery("SELECT DISTINCT a1_, a2_, a3_ FROM (org.intermine.model.testmodel.Company, org.intermine.model.testmodel.Bank) AS a1_, org.intermine.model.testmodel.Department AS a2_, org.intermine.model.testmodel.Broke AS a3_ WHERE (a1_.departments CONTAINS a2_ AND a1_.debtors CONTAINS a3_)", null));
        res.add(new IqlQuery("SELECT DISTINCT a1_, a2_, a3_ FROM (org.intermine.model.testmodel.Bank, org.intermine.model.testmodel.Company) AS a1_, org.intermine.model.testmodel.Department AS a2_, org.intermine.model.testmodel.Broke AS a3_ WHERE (a1_.departments CONTAINS a2_ AND a1_.debtors CONTAINS a3_)", null));
        results.put("DynamicClassRef3", res);
        res = new HashSet();
        res.add(new IqlQuery("SELECT DISTINCT a1_, a2_, a3_ FROM (org.intermine.model.testmodel.Company, org.intermine.model.testmodel.Bank) AS a1_, org.intermine.model.testmodel.Department AS a2_, org.intermine.model.testmodel.Broke AS a3_ WHERE (a2_.company CONTAINS a1_ AND a3_.bank CONTAINS a1_)", null));
        res.add(new IqlQuery("SELECT DISTINCT a1_, a2_, a3_ FROM (org.intermine.model.testmodel.Bank, org.intermine.model.testmodel.Company) AS a1_, org.intermine.model.testmodel.Department AS a2_, org.intermine.model.testmodel.Broke AS a3_ WHERE (a2_.company CONTAINS a1_ AND a3_.bank CONTAINS a1_)", null));
        results.put("DynamicClassRef4", res);
        res = new HashSet();
        res.add(new IqlQuery("SELECT DISTINCT a1_ FROM (org.intermine.model.testmodel.Employable, org.intermine.model.testmodel.Broke) AS a1_, (org.intermine.model.testmodel.HasAddress, org.intermine.model.testmodel.Broke) AS a2_ WHERE a1_ = a2_", null));
        res.add(new IqlQuery("SELECT DISTINCT a1_ FROM (org.intermine.model.testmodel.Broke, org.intermine.model.testmodel.Employable) AS a1_, (org.intermine.model.testmodel.HasAddress, org.intermine.model.testmodel.Broke) AS a2_ WHERE a1_ = a2_", null));
        res.add(new IqlQuery("SELECT DISTINCT a1_ FROM (org.intermine.model.testmodel.Employable, org.intermine.model.testmodel.Broke) AS a1_, (org.intermine.model.testmodel.Broke, org.intermine.model.testmodel.HasAddress) AS a2_ WHERE a1_ = a2_", null));
        res.add(new IqlQuery("SELECT DISTINCT a1_ FROM (org.intermine.model.testmodel.Broke, org.intermine.model.testmodel.Employable) AS a1_, (org.intermine.model.testmodel.Broke, org.intermine.model.testmodel.HasAddress) AS a2_ WHERE a1_ = a2_", null));
        results.put("DynamicClassConstraint", res);
        results.put("ContainsConstraintNull", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Employee AS a1_ WHERE a1_.address IS NULL", null));
        results.put("ContainsConstraintNotNull", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Employee AS a1_ WHERE a1_.address IS NOT NULL", null));
        results.put("SimpleConstraintNull", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Manager AS a1_ WHERE a1_.title IS NULL", null));
        results.put("SimpleConstraintNotNull", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Manager AS a1_ WHERE a1_.title IS NOT NULL", null));
        results.put("TypeCast", new IqlQuery("SELECT DISTINCT (a1_.age)::String AS a2_ FROM org.intermine.model.testmodel.Employee AS a1_", null));
        results.put("IndexOf", new IqlQuery("SELECT INDEXOF(a1_.name, 'oy') AS a2_ FROM org.intermine.model.testmodel.Employee AS a1_", null));
        results.put("Substring", new IqlQuery("SELECT SUBSTR(a1_.name, 2, 2) AS a2_ FROM org.intermine.model.testmodel.Employee AS a1_", null));
        results.put("Substring2", new IqlQuery("SELECT SUBSTR(a1_.name, 2) AS a2_ FROM org.intermine.model.testmodel.Employee AS a1_", null));
        results.put("OrderByReference", new IqlQuery("SELECT DISTINCT a1_ FROM org.intermine.model.testmodel.Employee AS a1_ ORDER BY a1_.department", null));
        results.put("FailDistinctOrder", new IqlQuery("SELECT DISTINCT a1_.name AS a2_ FROM org.intermine.model.testmodel.Employee AS a1_ ORDER BY a1_.age", null));
        results.put("LargeBagConstraint", NO_RESULT);
        results.put("LargeBagConstraintUsingTable", NO_RESULT);
        results.put("NegativeNumbers", new IqlQuery("SELECT a1_ FROM org.intermine.model.testmodel.Employee AS a1_ WHERE a1_.age > -51", null));
    }
}
