package edu.caltech.ch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.opencaesar.oml.AnnotationProperty;
import io.opencaesar.oml.Classifier;
import io.opencaesar.oml.ConceptInstance;
import io.opencaesar.oml.ConceptInstanceReference;
import io.opencaesar.oml.Description;
import io.opencaesar.oml.DescriptionStatement;
import io.opencaesar.oml.Element;
import io.opencaesar.oml.IdentifiedElement;
import io.opencaesar.oml.LinkAssertion;
import io.opencaesar.oml.Literal;
import io.opencaesar.oml.NamedInstance;
import io.opencaesar.oml.NamedInstanceReference;
import io.opencaesar.oml.Ontology;
import io.opencaesar.oml.Relation;
import io.opencaesar.oml.RelationEntity;
import io.opencaesar.oml.RelationInstance;
import io.opencaesar.oml.RelationInstanceReference;
import io.opencaesar.oml.ScalarProperty;
import io.opencaesar.oml.util.OmlRead;
import io.opencaesar.oml.util.OmlSearch;
import io.opencaesar.oml.util.OmlSwitch;

/**
 * The services class used by VSM.
 */
public class OmlServices {
    
	public static Set<DescriptionStatement> getAllDescriptionStatements(Description description) {
		var allDescriptions = new HashSet<Description>();
    	allDescriptions.add(description);
    	allDescriptions.addAll(
    			OmlRead.getAllImportedOntologies(description, false).stream()
    				.filter(o -> o instanceof Description)
    				.map(o -> (Description) o)
    				.collect(Collectors.toSet()));
    	var allStatements = new HashSet<DescriptionStatement>();
    	allDescriptions.stream().forEach(d -> {
    		allStatements.addAll(d.getOwnedStatements().stream()
    			.filter(i -> i instanceof DescriptionStatement)
    			.map(i -> (DescriptionStatement)i)
    			.collect(Collectors.toSet()));
    	});
    	return allStatements;
	}
	
	public static Set<NamedInstance> getAllNamedInstances(Description description) {
		var allNamedInstances = new HashSet<NamedInstance>();
    	getAllDescriptionStatements(description).stream().forEach(i -> {
    		NamedInstance ni = null;
    		if (i instanceof NamedInstance) {
    			ni = (NamedInstance) i;
    		} else if (i instanceof ConceptInstanceReference) {
    			ni = ((ConceptInstanceReference) i).getInstance();
    		} else if (i instanceof RelationInstanceReference) {
    			ni = ((RelationInstanceReference) i).getInstance();
    		}
    		if (ni != null) {
    			allNamedInstances.add(ni);
    		}
    	});
    	return allNamedInstances;
	}
	
    public static boolean isKindOf(NamedInstance instance, String kindName) {
    	var kind = (Classifier) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), kindName);
		return (kind != null) ? OmlSearch.findIsKindOf(instance, kind) : true;
	}

    public static boolean isTypeOf(NamedInstance instance, String kindName) {
    	var kind = (Classifier) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), kindName);
		return (kind != null) ? OmlSearch.findIsTypeOf(instance, kind) : false;
	}

    public static boolean isKindOf(LinkAssertion link, String relationName) {
    	var relation = (Relation) OmlRead.getMemberByAbbreviatedIri(link.getOntology(), relationName);
		return (relation != null) ? link.getRelation() == relation : false;
	}

    public static String getAnnotationValue(NamedInstance instance, String propertyName) {
    	var property = (AnnotationProperty) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), propertyName);
		var literal = (property != null) ? OmlRead.getAnnotationValue(instance, property) : null;
		return (literal != null) ? OmlRead.getStringValue(literal) : null;
	}

    public static boolean hasAnnotationValue(NamedInstance instance, String propertyName) {
    	var property = (AnnotationProperty) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), propertyName);
		var literal = (property != null) ? OmlRead.getAnnotationValue(instance, property) : null;
		return literal != null;
	}


    public static String getScalarValueIfAny(NamedInstance instance, String prefix, String propertyName) {
    	var property = (ScalarProperty) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), propertyName);
		var literal = (property != null) ? (Literal) OmlRead.getPropertyValue(instance, property) : null;
		return (literal != null) ? prefix+OmlRead.getStringValue(literal) : null;
	}
    
    public static String getScalarValue(NamedInstance instance, String propertyName) {
    	var property = (ScalarProperty) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), propertyName);
		var literal = (property != null) ? (Literal) OmlRead.getPropertyValue(instance, property) : null;
		return (literal != null) ? OmlRead.getStringValue(literal) : null;
	}

    public static boolean hasScalarValue(NamedInstance instance, String propertyName) {
    	var property = (ScalarProperty) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), propertyName);
		var literal = (property != null) ? (Literal) OmlRead.getPropertyValue(instance, property) : null;
		return literal != null;
	}

    public static String getTooltip(NamedInstance instance) {
    	return instance.getAbbreviatedIri();
    }

    public static NamedInstance resolveInstance(NamedInstanceReference reference) {
    	return (NamedInstance) OmlRead.resolve(reference);
    }

    public static NamedInstance getSource(LinkAssertion link) {
		return OmlRead.getSource(link);
	}

    public static NamedInstance getTarget(LinkAssertion link) {
		return OmlRead.getTarget(link);
	}

    public static NamedInstance getSource(RelationInstance instance) {
		return !instance.getSources().isEmpty() ? instance.getSources().get(0) : null;
	}

    public static NamedInstance getTarget(RelationInstance instance) {
		return !instance.getTargets().isEmpty() ? instance.getTargets().get(0) : null;
	}

    public static List<NamedInstance> getRelatedSourcesViaRelationInverseOf(NamedInstance instance, String relationyName) {
    	var relation = (Relation) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), relationyName);
    	final List<NamedInstance> targets = new ArrayList<>();
    	targets.addAll(
    			OmlSearch
    			.findLinkAssertionsWithTarget(instance).stream()
    			.filter(l -> l.getRelation() == relation)
    			.map(l -> OmlRead.getSource(l))
    			.collect(Collectors.toList()));
		return targets;
    }
    
    public static List<NamedInstance> getRelatedTargets(NamedInstance instance, String relationyName) {
    	var relation = (Relation) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), relationyName);
		return (relation != null) ? OmlSearch.findInstancesRelatedFrom(instance, relation) : Collections.emptyList();
	}

    public static List<NamedInstance> getAllRelatedTargets(NamedInstance instance, String relationyName) {
    	var relation = (Relation) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), relationyName);
		return (relation != null) ? OmlRead.closure(instance, true, i -> OmlSearch.findInstancesRelatedFrom(i, relation)) : Collections.emptyList();
    }
    
    public static List<NamedInstance> getRelatedSources(NamedInstance instance, String relationyName) {
    	var relation = (Relation) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), relationyName);
		return (relation != null) ? OmlSearch.findInstancesRelatedTo(instance, relation) : Collections.emptyList();
	}

    public static List<NamedInstance> getAllRelatedSources(NamedInstance instance, String relationyName) {
    	var relation = (Relation) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), relationyName);
		return (relation != null) ? OmlRead.closure(instance, true, i -> OmlSearch.findInstancesRelatedTo(i, relation)) : Collections.emptyList();
    }

    public static List<NamedInstance> getAllRelatedSourcesAndTargets(NamedInstance instance, String relationyName) {
    	final HashSet<NamedInstance> instances = new HashSet<>();
    	instances.addAll(getAllRelatedSources(instance, relationyName));
    	instances.addAll(getAllRelatedTargets(instance, relationyName));
    	final List<NamedInstance> result = new ArrayList<>();
    	result.addAll(instances);
    	return result;
    }
    
    public static List<RelationInstance> getRelationInstancesWithSource(NamedInstance instance, String relationyEntityName) {
    	final var relationEntity = (RelationEntity) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), relationyEntityName);
		List<RelationInstance> instances = OmlSearch.findRelationInstancesWithSource(instance);
		instances.removeIf(i -> !OmlSearch.findTypes(i).contains(relationEntity));
		return instances;
	}

    public static List<RelationInstance> getRelationInstancesWithTarget(NamedInstance instance, String relationyEntityName) {
    	final var relationEntity = (RelationEntity) OmlRead.getMemberByAbbreviatedIri(instance.getOntology().eResource().getResourceSet(), relationyEntityName);
		List<RelationInstance> instances = OmlSearch.findRelationInstancesWithTarget(instance);
		instances.removeIf(i -> !OmlSearch.findTypes(i).contains(relationEntity));
		return instances;
	}

	// Fix for OmlSearch.findInstancesRelatedTo
    public static List<NamedInstance> findInstancesRelatedTo(NamedInstance target, Relation relation) {
        final List<NamedInstance> sources = new ArrayList<>();
        sources.addAll(OmlSearch.findLinkAssertionsWithTarget(target).stream()
                .filter(a -> a.getRelation() == relation)
                .map(a -> OmlRead.getSource(a))
                .collect(Collectors.toList()));
        sources.addAll(OmlSearch.findRelationInstancesWithTarget(target).stream()
                .filter(i -> OmlSearch.findTypes(i).stream().filter(t -> ((RelationEntity)t).getForwardRelation() == relation).findFirst().isPresent())
                .flatMap(i -> i.getSources().stream())
                .collect(Collectors.toList()));
        return sources;
    }
   
    public static Set<NamedInstance> getLeafNamedInstancesOfKind(IdentifiedElement e, String kindName) {
    	if (e instanceof Description) {
    		return getOwnedRootNamedInstancesOfKind((Description) e, kindName);
    	} else if (e instanceof ConceptInstance) {
    		return getOwnedNamedInstancesOfKind((ConceptInstance) e, kindName);
    	} else
    		return Collections.emptySet();
    }
    
    public static Set<NamedInstance> getOwnedRootNamedInstancesOfKind(Description description, String kindName) {
    	var kind = (Classifier) OmlRead.getMemberByAbbreviatedIri(description.getOntology().eResource().getResourceSet(), kindName);
    	var bContains = (Relation) OmlRead.getMemberByAbbreviatedIri(description.getOntology().eResource().getResourceSet(), "base:contains");
    	var result = getAllNamedInstances(description).stream()
    			.filter(i -> 
    			OmlSearch.findIsTypeOf(i, kind) &&
    			findInstancesRelatedTo(i, bContains).isEmpty() &&
    			OmlSearch.findInstancesRelatedFrom(i, bContains).isEmpty())
    			.collect(Collectors.toSet());
    	return result;
    }

    public static Set<NamedInstance> getRootNonContainerNamedInstancesOfKind(Description description, String kindName) {
    	var kind = (Classifier) OmlRead.getMemberByAbbreviatedIri(description.getOntology().eResource().getResourceSet(), kindName);
    	var bContains = (Relation) OmlRead.getMemberByAbbreviatedIri(description.getOntology().eResource().getResourceSet(), "base:contains");
    	return getAllNamedInstances(description).stream()
    			.filter(i -> 
    			OmlSearch.findIsTypeOf(i, kind) &&
    			findInstancesRelatedTo(i, bContains).isEmpty() &&
    			OmlSearch.findInstancesRelatedFrom(i, bContains).isEmpty())
    			.collect(Collectors.toSet());
    }

    public static Set<NamedInstance> getContainerNamedInstancesOfKind(Element e, String kindName) {
    	if (e instanceof Description) {
    		return getContainerNamedInstancesOfKind((Description) e, kindName);
    	} else if (e instanceof ConceptInstance) {
    		return getOwnedNamedInstancesOfKind((ConceptInstance) e, kindName);
    	} else if (e instanceof ConceptInstanceReference) {
    		return getOwnedNamedInstancesOfKind(((ConceptInstanceReference) e).getInstance(), kindName);
    	} else
    		return Collections.emptySet();
    }
    
    public static Set<NamedInstance> getContainerNamedInstancesOfKind(Description description, String kindName) {
    	var kind = (Classifier) OmlRead.getMemberByAbbreviatedIri(description.getOntology().eResource().getResourceSet(), kindName);
    	var bContains = (Relation) OmlRead.getMemberByAbbreviatedIri(description.getOntology().eResource().getResourceSet(), "base:contains");
    	return getAllNamedInstances(description).stream()
    			.filter(i -> 
    			OmlSearch.findIsTypeOf(i, kind) &&
    			findInstancesRelatedTo(i, bContains).isEmpty() &&
    			!OmlSearch.findInstancesRelatedFrom(i, bContains).isEmpty())
    			.collect(Collectors.toSet());
    }

    public static Set<NamedInstance> getContainerNamedInstancesOfKind(ConceptInstance instance, String kindName) {
    	var description = (Description) instance.getOntology();
    	var kind = (Classifier) OmlRead.getMemberByAbbreviatedIri(description.getOntology().eResource().getResourceSet(), kindName);
    	var bContains = (Relation) OmlRead.getMemberByAbbreviatedIri(description.getOntology().eResource().getResourceSet(), "base:contains");
		
    	return getAllNamedInstances(description).stream()
    			.filter(i -> 
    			OmlSearch.findIsTypeOf(i, kind) &&
    			findInstancesRelatedTo(i, bContains).isEmpty() &&
    			!OmlSearch.findInstancesRelatedFrom(i, bContains).isEmpty())
    			.collect(Collectors.toSet());
    }
    public static Set<NamedInstance> getOwnedNamedInstancesOfKind(NamedInstance parent, String kindName) {
    	var description = (Description) parent.getOntology();
    	var kind = (Classifier) OmlRead.getMemberByAbbreviatedIri(description.eResource().getResourceSet(), kindName);
    	var bContains = (Relation) OmlRead.getMemberByAbbreviatedIri(description.eResource().getResourceSet(), "base:contains");
    	return getAllNamedInstances(description).stream()
    			.filter(i -> 
    			OmlSearch.findIsTypeOf(i, kind) &&
    			findInstancesRelatedTo(i, bContains).contains(parent))
    			.collect(Collectors.toSet());
    }

    public static Set<NamedInstance> getInputNamedInstancesOfKind(NamedInstance parent, String kindName, String inputName) {
    	var description = (Description) parent.getOntology();
    	var kind = (Classifier) OmlRead.getMemberByAbbreviatedIri(description.eResource().getResourceSet(), kindName);
    	var input = (Relation) OmlRead.getMemberByAbbreviatedIri(description.eResource().getResourceSet(), inputName);
    	Set<NamedInstance> candidates = getAllNamedInstances(description).stream()
    			.filter(i -> 
    			OmlSearch.findIsTypeOf(i, kind))
    			.collect(Collectors.toSet());
    	Set<NamedInstance> inputs = candidates
    			.stream()
    			.filter(i -> OmlSearch.findInstancesRelatedFrom(i, input).contains(parent))
    			.collect(Collectors.toSet());
    	return inputs;
    }
    
    public static Set<NamedInstance> getOutputNamedInstancesOfKind(NamedInstance parent, String kindName, String outputName) {
    	var description = (Description) parent.getOntology();
    	var kind = (Classifier) OmlRead.getMemberByAbbreviatedIri(description.eResource().getResourceSet(), kindName);
    	var output = (Relation) OmlRead.getMemberByAbbreviatedIri(description.eResource().getResourceSet(), outputName);
    	Set<NamedInstance> candidates = getAllNamedInstances(description).stream()
    			.filter(i -> 
    			OmlSearch.findIsTypeOf(i, kind))
    			.collect(Collectors.toSet());
    	Set<NamedInstance> outputs = candidates
    			.stream()
    			.filter(i -> OmlSearch.findInstancesRelatedFrom(parent, output).contains(i))
    			.collect(Collectors.toSet());
    	return outputs;
    }
    
    public static Set<NamedInstance> getOwnedNamedInstances(Description description) {
    	return getAllNamedInstances(description).stream().collect(Collectors.toSet());
    }
    
    public static Set<NamedInstanceReference> getOwnedNamedInstanceReferences(Description description) {
    	return getAllDescriptionStatements(description).stream()
    			.filter(i -> i instanceof NamedInstanceReference)
    			.map(i -> (NamedInstanceReference)i)
    			.collect(Collectors.toSet());
    }

    public static Set<NamedInstance> getNamedInstancesInContext(Description description, boolean includeImports) {
		var allDescriptions = new HashSet<Ontology>();
		allDescriptions.add(description);
		
		if (includeImports) {
    		allDescriptions.addAll(
    			OmlRead.getAllImportedOntologies(description, false).stream()
    				.filter(o -> o instanceof Description)
    				.collect(Collectors.toSet()));
    	}
    	
    	var instances = new LinkedHashSet<NamedInstance>();
    	var visitor = new OmlSwitch<Void>() {
			@Override
			public Void caseLinkAssertion(LinkAssertion object) {
				instances.add(object.getTarget());
				return null;
			}
			@Override
			public Void caseConceptInstance(ConceptInstance object) {
				instances.add(object);
				return null;
			}
			@Override
			public Void caseConceptInstanceReference(ConceptInstanceReference object) {
				instances.add((ConceptInstance)OmlRead.resolve(object));
				return null;
			}
			@Override
			public Void caseRelationInstance(RelationInstance object) {
				instances.add(object);
				instances.addAll(object.getSources());
				instances.addAll(object.getTargets());
				return null;
			}
			@Override
			public Void caseRelationInstanceReference(RelationInstanceReference object) {
				instances.add((RelationInstance)OmlRead.resolve(object));
				return null;
			}
    	};
    	allDescriptions.forEach(d -> d.eAllContents().forEachRemaining(i -> visitor.doSwitch(i)));
    	return instances;
	}
    
}
