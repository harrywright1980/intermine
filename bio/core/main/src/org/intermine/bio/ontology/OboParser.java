package org.intermine.bio.ontology;

/*
 * Copyright (C) 2002-2009 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.dataadapter.OBOSerializationEngine;
import org.obo.dataadapter.SimpleLinkFileAdapter;
import org.obo.datamodel.OBOSession;

/**
 * @author Thomas Riley
 * @author Peter Mclaren - 5/6/05 - added some functionality to allow terms to find all their parent
 * @author Xavier Watkins - 06/01/09 - refactored model
 * terms.
 */
public class OboParser
{
    private static final Logger LOG = Logger.getLogger(OboParser.class);

    private final Pattern synPattern = Pattern.compile("\\s*\"(.+?[^\\\\])\".*");
    private final Matcher synMatcher = synPattern.matcher("");
    
    
    /**
     * All terms.
     */
    protected Map<String, OboTerm> terms = new HashMap<String, OboTerm>();
    
    /**
     * All relations
     */
    protected List<OboRelation> relations = new ArrayList<OboRelation>();
    
    /**
     * All relation types
     */
    protected Map<String, OboTypeDefinition> types = new HashMap<String, OboTypeDefinition>();

    
    /**
     * Default namespace.
     */
    protected String defaultNS = "";

    /**
     * Parse an OBO file to produce a set of OboTerms.
     * @param in with text in OBO format
     * @throws Exception if anything goes wrong
     */
    public void processOntology(Reader in) throws Exception {
        readTerms(new BufferedReader(in));
    }
    
    /**
     * Parse the relations file generated by the OboEdit reasoner (calculates transitivity)
     * 
     * @param dagFileName the name of the obo file to read from
     * @throws Exception if something goes wrong
     */
    public void processRelations(String dagFileName) throws Exception {
        File temp = File.createTempFile("links", ".txt");
        // Copied from OBO2Linkfile.convertFiles(OBOAdapterConfiguration, OBOAdapterConfiguration,
        // List); OBOEDIT code
        // TODO OBO will soon release the file containing all transitive closures calculated 
        // by obo2linkfile so we can get rid of the code below and just use the downloaded file.
        OBOFileAdapter.OBOAdapterConfiguration readConfig = 
            new OBOFileAdapter.OBOAdapterConfiguration();
        
        readConfig.setBasicSave(false);
        readConfig.getReadPaths().add(dagFileName);
        
        OBOFileAdapter.OBOAdapterConfiguration writeConfig = 
            new OBOFileAdapter.OBOAdapterConfiguration();
        writeConfig.setBasicSave(false);
        
        OBOSerializationEngine.FilteredPath path = new OBOSerializationEngine.FilteredPath();
        path.setUseSessionReasoner(false);
        path.setImpliedType(OBOSerializationEngine.SAVE_ALL);
        path.setPath(temp.getName());
        writeConfig.getSaveRecords().add(path);

        writeConfig.setSerializer("OBO_1_2");
        
        OBOFileAdapter adapter = new OBOFileAdapter();
        OBOSession session = (OBOSession) adapter.doOperation(OBOAdapter.READ_ONTOLOGY,
                readConfig, null);
        SimpleLinkFileAdapter writer = new SimpleLinkFileAdapter();
        
        writer.doOperation(OBOAdapter.WRITE_ONTOLOGY, writeConfig, session);
        //
        LOG.info("PROGRESS" + writer.getProgressString());
        // END OF OBO2EDIT code
        readRelations(new BufferedReader(new FileReader(temp.getName())));
        temp.delete();
    }

    /**
     * Parse an OBO file to produce a map from ontology term id to name.
     *
     * @param in text in OBO format
     * @return a map from ontology term identifier to name
     * @throws IOException if anything goes wrong
     */
    public Map<String, String> getTermIdNameMap(Reader in) throws IOException {
        readTerms(new BufferedReader(in));
        Map<String, String> idNames = new HashMap<String, String>();
        for (OboTerm ot : terms.values()) {
            idNames.put(ot.getId(), ot.getName());
        }
        return idNames;
    }
    
    /**
     * @return a set of DagTerms
     */
    public Set getOboTerms() {
        return new HashSet(terms.values());
    }
    
    /**
     * @return a list of OboRelations
     */
    public List<OboRelation> getOboRelations() {
        return relations;
    }


    /**
     * Read DAG input line by line to generate hierarchy of DagTerms.
     *
     * @param in text in DAG format
     * @throws IOException if anything goes wrong
     */
    public void readTerms(BufferedReader in) throws IOException {
        String line;
        Map tagValues = new MultiValueMap();
        List<Map> termTagValuesList = new ArrayList<Map>();
        List<Map> typeTagValuesList = new ArrayList<Map>();
        
        Pattern tagValuePattern = Pattern.compile("(.+?[^\\\\]):(.+)");
        Pattern stanzaHeadPattern = Pattern.compile("\\s*\\[(.+)\\]\\s*");
        Matcher tvMatcher = tagValuePattern.matcher("");
        Matcher headMatcher = stanzaHeadPattern.matcher("");

        while ((line = in.readLine()) != null) {
            // First strip off any comments
            if (line.indexOf('!') >= 0) {
                line = line.substring(0, line.indexOf('!'));
            }

            tvMatcher.reset(line);
            headMatcher.reset(line);

            if (headMatcher.matches()) {
                String stanzaType = headMatcher.group(1);
                tagValues = new MultiValueMap(); // cut loose
                if (stanzaType.equals("Term")) {
                    termTagValuesList.add(tagValues);
                    LOG.debug("recorded term with " + tagValues.size() + " tag values");
                } else
                    if (stanzaType.equals("Typedef")) {
                        typeTagValuesList.add(tagValues);
                        LOG.debug("recorded type with " + tagValues.size() + " tag values");
                    } else {
                        LOG.warn("Ignoring " + stanzaType + " stanza");
                    }
                LOG.debug("matched stanza " + stanzaType);
            } else if (tvMatcher.matches()) {
                String tag = tvMatcher.group(1).trim();
                String value = tvMatcher.group(2).trim();
                tagValues.put(tag, value);
                LOG.debug("matched tag \"" + tag + "\" with value \"" + value + "\"");

                if (tag.equals("default-namespace")) {
                    defaultNS = value;
                    LOG.info("default-namespace is \"" + value + "\"");
                }
            }
        }

        in.close();

        //LOG.info("Found " + tagValuesList.size() + " root terms");
        
        // Build the OboTypeDefinition objects
        OboTypeDefinition oboType = new OboTypeDefinition("is_a", "is_a", true);
        types.put(oboType.getId() , oboType);
        for (Iterator iter = typeTagValuesList.iterator(); iter.hasNext();) {
            Map tvs = (Map) iter.next();
            String id = (String) ((List) tvs.get("id")).get(0);
            String name = (String) ((List) tvs.get("name")).get(0);
            boolean isTransitive = isTrue(tvs, "is_transitive");
            oboType = new OboTypeDefinition(id, name, isTransitive);
            types.put(oboType.getId() , oboType);
        }
        
        // Just build all the OboTerms disconnected
        for (Iterator iter = termTagValuesList.iterator(); iter.hasNext();) {
            Map tvs = (Map) iter.next();
            String id = (String) ((List) tvs.get("id")).get(0);
            String name = (String) ((List) tvs.get("name")).get(0);
            OboTerm term = new OboTerm(id, name);
            term.setObsolete(isTrue(tvs, "is_obsolete"));
            terms.put(term.getId(), term);
        }

        // Now connect them all together
        for (Iterator iter = termTagValuesList.iterator(); iter.hasNext();) {
            Map tvs = (Map) iter.next();
            if (!isTrue(tvs, "is_obsolete")) {
                configureDagTerm(tvs);
            }
        }
    }

    /**
     * Configure dag terms with values from one entry.
     *
     * @param tagValues term config
     */
    protected void configureDagTerm(Map tagValues) {
        String id = (String) ((List) tagValues.get("id")).get(0);
        OboTerm term = terms.get(id);

        if (term != null) {
            term.setTagValues(tagValues);

            List synonyms = (List) tagValues.get("synonym");
            if (synonyms != null) {
                addSynonyms(term, synonyms, "synonym");
            }
            synonyms = (List) tagValues.get("related_synonym");
            if (synonyms != null) {
                addSynonyms(term, synonyms, "related_synonym");
            }
            synonyms = (List) tagValues.get("exact_synonym");
            if (synonyms != null) {
                addSynonyms(term, synonyms, "exact_synonym");
            }
            synonyms = (List) tagValues.get("broad_synonym");
            if (synonyms != null) {
                addSynonyms(term, synonyms, "broad_synonym");
            }
            synonyms = (List) tagValues.get("narrow_synonym");
            if (synonyms != null) {
                addSynonyms(term, synonyms, "narrow_synonym");
            }

            // Set namespace
            List nsl = (List) tagValues.get("namespace");
            if (nsl != null && nsl.size() > 0) {
                term.setNamespace((String) nsl.get(0));
            } else {
                term.setNamespace(defaultNS);
            }

            // Set description
            List defl = (List) tagValues.get("def");
            String def = null;
            if (defl != null && defl.size() > 0) {
                def = (String) defl.get(0);
                synMatcher.reset(def);
                if (synMatcher.matches()) {
                    term.setDescription(unescape(synMatcher.group(1)));
                }
            } else {
                LOG.warn("Failed to parse def of term " + id + " def: " + def);
            }

        } else {
            LOG.warn("OboParser.configureDagTerm() - no term found for id:" + id);
        }
    }

    /**
     * Given the tag+value map for a term, return whether it's true or false
     *
     * @param tagValues map of tag name to value for a single term
     * @param tagValue the term to look for in the map
     * @return true if the term is marked true, false if not
     */
    public static boolean isTrue(Map tagValues, String tagValue) {
        List vals = (List) tagValues.get(tagValue);
        if (vals != null && vals.size() > 0) {
            if (vals.size() > 1) {
                LOG.warn("Term: " + tagValues + " has more than one (" + vals.size()
                        + ") is_obsolete values - just using first");
            }
            return ((String) vals.get(0)).equalsIgnoreCase("true");
        }
        return false;
    }

    /**
     * Add synonyms to a DagTerm.
     *
     * @param term     the DagTerm
     * @param synonyms List of synonyms (Strings)
     * @param type     synonym type
     */
    protected void addSynonyms(OboTerm term, List synonyms, String type) {
        for (Iterator iter = synonyms.iterator(); iter.hasNext();) {
            String line = (String) iter.next();
            synMatcher.reset(line);
            if (synMatcher.matches()) {
                term.addSynonym(new OboTermSynonym(unescape(synMatcher.group(1)), type));
            } else {
                LOG.error("Could not match synonym value from: " + line);
            }
        }
    }
    
    /**
     * This method reads relations calculated by the GO2Link script in OBOEdit.
     * 
     * 
     * @param in the reader for the Go2Link file
     * @throws IOException an exception
     */
    protected void readRelations(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            String[] bits = line.split("\t");
            OboTypeDefinition type = types.get(bits[1].replaceAll("OBO_REL:", ""));
            if (type != null) {
                String id1 = null, id2 = null;
                boolean asserted = false, redundant = false;
                for (int i = 0; i < bits.length; i++) {
                    switch (i) {
                    case 0:// id1
                        {
                            id1 = bits[i];
                            break;
                        }
                    case 1:// type
                        {
                            // already initialised
                            break;
                        }
                    case 2:// id2
                        {
                            id2 = bits[i];
                            break;
                        }
                    case 3:// asserted
                        {
                            asserted = (bits[i]).matches("asserted");
                            break;
                        }
                    case 4:// ??
                        {
                            // do nothing
                            break;
                        }
                    case 5:// redundant
                        {
                            redundant = (bits[i]).matches("redundant");
                            break;
                        }
                    }
                }
                OboRelation relation = new OboRelation(id1, id2, type);
                relation.setDirect(asserted);
                relation.setRedundant(redundant);
                relations.add(relation);
            } else {
                LOG.info("Unsupported type:" + bits[1]);
            }
        }
        in.close();
    }

    /**
     * Perform OBO unescaping.
     *
     * @param string the escaped string
     * @return the corresponding unescaped string
     */
    protected String unescape(String string) {
        int sz = string.length();
        StringBuffer out = new StringBuffer(sz);
        boolean hadSlash = false;

        for (int i = 0; i < sz; i++) {
            char ch = string.charAt(i);

            if (hadSlash) {
                switch (ch) {
                    case 'n':
                        out.append('\n');
                        break;
                    case 't':
                        out.append('\t');
                        break;
                    case 'W':
                        out.append(' ');
                        break;
                    default:
                        out.append(ch);
                        break;
                }
                hadSlash = false;
            } else if (ch == '\\') {
                hadSlash = true;
            } else {
                out.append(ch);
            }
        }

        return out.toString();
    }
}
