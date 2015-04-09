/*
 * generated by Xtext
 */
package no.hib.dpf.text.ui.contentassist;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import no.hib.dpf.text.DPFConstants;
import no.hib.dpf.text.scala.DPFTextCore;
import no.hib.dpf.text.tdpf.Constraint;
import no.hib.dpf.text.tdpf.ConstraintList;
import no.hib.dpf.text.tdpf.Node;
import no.hib.dpf.text.tdpf.Signature;
import no.hib.dpf.text.util.CommonUtil;
import no.hib.dpf.text.util.Tuple;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;

/**
 * DPFTextProposalProvider calculates proposals for auto-completion.
 * Note: what you get from the parser is not very reliable, therefore detection for which 
 * auto-completion function should be called have been implemented mostly by using regular expressions.  
 */
public class DPFTextProposalProvider extends AbstractDPFTextProposalProvider {
    
	/**
	 * Different proposal cases, which may be detected.
	 */
	private enum SG {
		ARROW, TARROW, 
		SNODE, TSNODE, 
		SNODE_TRG, 
		TSNODE_TRG,  
		PROPERTY, TPROPERTY, PROPERTY_TRG, TPROPERTY_TRG, OTHER
	};

    private static Pattern SNODE_TRG = Pattern.compile(".*:.*-.*:.*->");
    private static Pattern SNODE_extends = Pattern.compile(".* extends");	
    private static Pattern SNODE_extends_simple = Pattern.compile(".*:.*-\\|>");
    private static Pattern SNODE_and = Pattern.compile(".* and");	
    private static Pattern TSNODE_extends = Pattern.compile(".* extends .*:");	
    private static Pattern TSNODE_extends_simple = Pattern.compile(".*:.*-\\|>.*:");
    private static Pattern TSNODE_and = Pattern.compile(".* and .*:");	
    
    private static Pattern TSNODE_TRG = Pattern.compile(".*:.*-.*:.*->.*:");
    private static Pattern TSNODE = Pattern.compile(".*:"); //match late since pattern is quite general
    
    private static Pattern ARROW = Pattern.compile(".*:.*-");    
    private static Pattern TARROW = Pattern.compile(".*:.*-.*:"); //after SNODE_TRG

    private static Pattern PROPERTY_TRG = Pattern.compile(".*:.*->");
    private static Pattern TPROPERTY_TRG = Pattern.compile(".*:.*->.*:");
    private static Pattern TPROPERTY = Pattern.compile(".*:"); //match late since pattern is quite general
    
    private static Pattern IN_NODE_1 = Pattern.compile(".*:.*\\{");    
    private static Pattern OUT_NODE  = Pattern.compile(".*:.*-.*:.*->.*,");    
    private static Pattern IN_NODE_2 = Pattern.compile(".*:.*->.*:.*,"); 

    /**
	 * For which element proposals should be generated.
	 * 
	 * @param e
	 * @param context
	 * @return
	 */
	private Tuple<SG, String> proposalsForWhichElement(final EObject e, final ContentAssistContext context) {
		try {
			//Get part which need to be completed:
			final String docUntilCompletion = context.getDocument().get(0, context.getOffset());			
			final int beginBracket = docUntilCompletion.lastIndexOf(',');
			final int beginComma = docUntilCompletion.lastIndexOf('{');
			final int beginOfElement = Math.max(beginBracket, beginComma);
			
			final String detectionString = docUntilCompletion.substring(beginOfElement + 1).trim();		

			//
			//ATTENTION: Sequence of checks is important!
			//		
			
			//
			//For both cases:
			//
			if(SNODE_extends.matcher(detectionString).matches()){
				return new Tuple<SG, String>(SG.SNODE,detectionString);
			}
			if(SNODE_and.matcher(detectionString).matches()){
				return new Tuple<SG, String>(SG.SNODE,detectionString);
			}
			if(TSNODE_extends.matcher(detectionString).matches()){
				return new Tuple<SG, String>(SG.TSNODE,detectionString);
			}
			if(TSNODE_and.matcher(detectionString).matches()){
				return new Tuple<SG, String>(SG.TSNODE,detectionString);
			}
			if(SNODE_extends_simple.matcher(detectionString).matches()){
				return new Tuple<SG, String>(SG.SNODE,detectionString);
			}
			if(TSNODE_extends_simple.matcher(detectionString).matches()){
				return new Tuple<SG, String>(SG.TSNODE,detectionString);
			}

			//
			//Am I inside "X:Node{.."?
			//Unfortunately: model and model.sContainer gives in different parses ambiguous information! 
			//
			//Get last complete element
			final String docUntilPrevious = docUntilCompletion.substring(0, beginOfElement);
			final int beginBracketPrev = docUntilPrevious.lastIndexOf(',');
			final int beginCommaPrev = docUntilPrevious.lastIndexOf('{');
			final int beginOfElementPrev = Math.max(beginBracketPrev, beginCommaPrev);
			
			final String previousDetectionString = docUntilPrevious.substring(beginOfElementPrev + 1).trim() + docUntilCompletion.charAt(beginOfElement);		
			
			boolean inNode = false;
			if(IN_NODE_1.matcher(previousDetectionString).matches() ||
			  (!OUT_NODE.matcher(previousDetectionString).matches() && IN_NODE_2.matcher(previousDetectionString).matches())		
			){
				inNode=true;
			}
			
			//DPFTextCore.log("inNode =" + inNode); 
			
			//
			//Node in "class" presentation:
			//
			if(inNode){
				if(detectionString.isEmpty()){
					return new Tuple<SG, String>(SG.PROPERTY,detectionString);
				}				
				if(PROPERTY_TRG.matcher(detectionString).matches()){
					return new Tuple<SG, String>(SG.PROPERTY_TRG,detectionString);
				}
				if(TPROPERTY_TRG.matcher(detectionString).matches()){
					return new Tuple<SG, String>(SG.TPROPERTY_TRG,detectionString);
				}						
				if(TPROPERTY.matcher(detectionString).matches()){
					return new Tuple<SG, String>(SG.TPROPERTY,detectionString);
				}		
			}	
			//
			//Simple presentation
			//
			else {
				if(detectionString.isEmpty()){
					return new Tuple<SG, String>(SG.SNODE,detectionString);
				}
				if(SNODE_TRG.matcher(detectionString).matches()){
					return new Tuple<SG, String>(SG.SNODE_TRG,detectionString);
				}
				if(TSNODE_TRG.matcher(detectionString).matches()){
					return new Tuple<SG, String>(SG.TSNODE_TRG,detectionString);
				}
				if(ARROW.matcher(detectionString).matches()){
					return new Tuple<SG, String>(SG.ARROW,detectionString);
				}
				if(TARROW.matcher(detectionString).matches()){
					return new Tuple<SG, String>(SG.TARROW,detectionString);					
				}
				if(TSNODE.matcher(detectionString).matches()){
					return new Tuple<SG, String>(SG.TSNODE,detectionString);
				}
			}
		
			return new Tuple<SG, String>(SG.OTHER,detectionString);	
			
		} catch (Exception ex) {
			DPFTextCore.log(ex);
			return new Tuple<SG, String>(SG.OTHER,ex.getMessage());
		}
	}
	
	/**
	 * Complete a DPFId (i.e name and RId). 
	 * Here many different semantic cases have to be considered.
	 */
	@Override
	public void complete_DpfId(EObject model, RuleCall ruleCall, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		try {
			final Tuple<SG, String> rs = proposalsForWhichElement(model, context);			
			
			//DPFTextCore.log(model.toString() + " / " + rs.toString()); 
			
			List<String> proposals = Collections.<String> emptyList();
			final String[] namePath = getNamePath(model);
			
			switch (rs.x) {
			case ARROW:{	
				//Complete an arrow inside the constraint section of a specification:
				if (model instanceof Constraint || model.eContainer() instanceof Constraint) {
					final String srcNode = rs.y.substring(0,rs.y.indexOf(':'));
					final String[] s = splitStringToIdName(srcNode);
					proposals = DPFTextCore.getProposal_CompleteArrows(s[0], getId(s), namePath[0], namePath[1]);
				}	
 			    break; 		
			}
			case TARROW:
			{	
				final String srcTNode = rs.y.substring(rs.y.indexOf(':')+1,rs.y.indexOf('-'));
				final String[] s = splitStringToIdName(srcTNode);
				proposals = DPFTextCore.getProposal_ArrowTypes(s[0], getId(s), namePath[0], namePath[1]);
			}	
			break;
			case SNODE:
			{	
				proposals = DPFTextCore.getProposal_AllNodes(namePath[0], namePath[1]);
			}	
			break;
			case TSNODE:
			{	
				proposals = DPFTextCore.getProposal_AllNodeTypes(namePath[0], namePath[1]);
			}	
			break;
			case SNODE_TRG:
			{
				final String srcNodeType = rs.y.substring(rs.y.indexOf(':')+1,rs.y.indexOf('-'));
				final String[] s1 = splitStringToIdName(srcNodeType);
				final String arrowType = rs.y.substring(rs.y.indexOf(':',rs.y.indexOf(':')+1)+1,rs.y.lastIndexOf('-'));
				final String[] s2 = splitStringToIdName(arrowType);
				proposals = DPFTextCore.getProposal_ArrowTargetNodes(s1[0], getId(s1), s2[0], getId(s2), namePath[0], namePath[1]);
			}	
			break;
			case TSNODE_TRG:
			{	
				final String srcNodeType = rs.y.substring(rs.y.indexOf(':')+1,rs.y.indexOf('-'));
				final String[] s1 = splitStringToIdName(srcNodeType);
				final String arrowType = rs.y.substring(rs.y.indexOf(':',rs.y.indexOf(':')+1)+1,rs.y.lastIndexOf('-'));
				final String[] s2 = splitStringToIdName(arrowType);
				proposals = DPFTextCore.getProposal_ArrowTargetTypes(s1[0], getId(s1), s2[0], getId(s2), namePath[0], namePath[1]);
			}	
			break;
			case PROPERTY:	break; //no proposal for Property	
			case TPROPERTY:	
			{	
				final Node n = getNode(model);				
				if(null != n){				
					proposals = DPFTextCore.getProposal_ArrowTypes(n.getType().getName(),new HashSet<Integer>(n.getType().getId().getNums()), namePath[0], namePath[1]);
				}
			}	
			break;
			case PROPERTY_TRG:
			{	
				final Node n = getNode(model);				
				if(null != n){				
					final String propertyType = rs.y.substring(rs.y.indexOf(':')+1,rs.y.indexOf('-'));
					final String[] s = splitStringToIdName(propertyType);
					proposals = DPFTextCore.getProposal_ArrowTargetNodes(n.getType().getName(),new HashSet<Integer>(n.getType().getId().getNums()), s[0], getId(s), namePath[0], namePath[1]);	
				}
			}	
			break;
			case TPROPERTY_TRG:
			{	
				final Node n = getNode(model);				
				if(null != n){				
					final String propertyType = rs.y.substring(rs.y.indexOf(':')+1,rs.y.indexOf('-'));
					final String[] s = splitStringToIdName(propertyType);
					proposals = DPFTextCore.getProposal_ArrowTargetTypes(n.getType().getName(),new HashSet<Integer>(n.getType().getId().getNums()), s[0], getId(s), namePath[0], namePath[1]);				
				}
			}	
			break;
			case OTHER:
				DPFTextCore.log(model.toString() + " / " + rs.toString());
			}

			// Add all proposals:
	    	if(!proposals.isEmpty()){
				for (String ps : proposals) {
					acceptor.accept(super.createCompletionProposal(ps, context));
				}
			}			
		} catch (Exception e) {
			DPFTextCore.log(e);
		}
	}
	
	/**
	 * Filter some unwanted (Xtext-generated) proposals:
	 */
	@Override
	protected boolean isValidProposal(String proposal, String prefix, ContentAssistContext context) {
		if (super.isValidProposal(proposal, prefix, context)) {
			return !proposal.trim().equals("1") &&       // One is not a good proposal in any context
				   !proposal.trim().equals("\"Value\""); //Do not propose "Value"!	
		}
		return false;
	}
	
	/**
	 * Calculate the name and path for the current specification
	 * 
	 * @param o
	 * @return
	 */
	private String[] getNamePath(EObject o) {
		final String[] rs = new String[2];
		try {
			final IFile model =  CommonUtil.eObject2IFile(o);
			final String fname = model.getName();
			final String name = fname.substring(0, fname.indexOf('_'));
			final String path = model.getParent().getFullPath().toOSString();
			// System.out.println("Name=" + name);
			// System.out.println("Path=" + path);
			rs[0] = name;
			rs[1] = path;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}

	/**
	 * Split a Id string ABC(AT)1,2,3 into ABC and 1,2,3.
	 * @param s
	 */
	private String[] splitStringToIdName(String s) {
		int f = s.indexOf('@');
		if (-1 < f) {
			final StringTokenizer t = new StringTokenizer(s, "@");
			final String name = t.nextToken().trim();
			final String id = t.nextToken().trim();
			// System.out.println("name=" + name);
			// System.out.println("id=" + id);
			return new String[] { name, id };
		} else {
			// System.out.println("name=" + s);
			return new String[] { s.trim(), "" };
		}
	}

	/**
	 * Create a "RId" as set of integers
	 * @param s
	 */
	private Set<Integer> getId(String[] a) {
		try {
			// System.out.println("getId(String[] a):>" + Arrays.toString(a));
			final StringTokenizer t = new StringTokenizer(a[1], ",");
			final HashSet<Integer> rs = new HashSet<Integer>();
			while (t.hasMoreTokens()) {
				rs.add(Integer.valueOf(t.nextToken()));
			}
			return rs;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get the container node of an element if there is one.
	 * @param o
	 * @return
	 */
	private Node getNode(EObject o){
		try {
			if(o instanceof Node){
				return (Node)o;
			}else{
				return getNode(o.eContainer());
			}			
		} catch (Exception e) {
			//Node could not be detected!
			return null;
		}
	}
	

	// ***************************************************************************************

    /**
     * Propose all possible constraints in a specifications constraint section
     */
	@Override
	public void complete_Constraint(EObject model, RuleCall ruleCall, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		try {
			if (model instanceof ConstraintList) {
				final ConstraintList cs = (ConstraintList) model;
				final IContainer dir =  CommonUtil.eObject2IFile(model).getParent();
				final Signature s =  DPFTextCore.readSignature(cs.getConstraintSemantic().getId(), dir);
				for (String c : DPFTextCore.getProposal_AllConstraints(s)) {
					acceptor.accept(super.createCompletionProposal(c, context));
				}
			} else {
				super.complete_Constraint(model, ruleCall, context, acceptor);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Remove Id proposal
	 */
	@Override
	public void complete_ID(EObject model, RuleCall ruleCall, final ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
	}

	/**
	 * Propose specifications as meta-specification
	 */
	@Override
	public void complete_ChoosenSpecification(EObject model, RuleCall ruleCall, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		try {
			final IContainer dir =  CommonUtil.eObject2IFile(model).getParent();
			for (String c : DPFTextCore.getProposal_AllSpecifications(dir)) {
				acceptor.accept(super.createCompletionProposal(c, context));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * Propose signatures in the constraint section of a specification
	 */
	@Override
	public void complete_ChoosenSignature(EObject model, RuleCall ruleCall, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		try {
			final IContainer dir =  CommonUtil.eObject2IFile(model).getParent();
			for (String c : DPFTextCore.getProposal_AllSignatures(dir)) {
				acceptor.accept(super.createCompletionProposal(c, context));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Propose specifications in the batch file command as input for the command.
	 */
	@Override
	public void completeMakeEmf_Id(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		proposeSpecifications(model, context, acceptor);
	}
	
	/**
	 * Propose specifications in the batch file command as input for the command.
	 */
	@Override
	public void completeMakeEcore_Id(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		proposeSpecifications(model, context, acceptor);
	}
	
	/**
	 * Propose specifications in the batch file command as input for the command.
	 */
	@Override
	public void completeMakeImage_Id(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		proposeSpecifications(model, context, acceptor);
	}

	/**
	 * Propose specifications in the batch file command as input for the command.
	 */
	@Override
	public void completeMakeDiagram_Id(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		proposeSpecifications(model, context, acceptor);
	}
	
	/**
	 * Propose specifications in the batch file command as input for the command.
	 */
	@Override
	public void completeTestMatch_Search(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		proposeSpecifications(model, context, acceptor);
	}
	
	/**
	 * Propose specifications in the batch file command as input for the command.
	 */
	@Override
	public void completeTestMatch_Into(EObject model, Assignment assignment, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		proposeSpecifications(model, context, acceptor);
	}

	/**
	 * Propose specifications 
	 */
	private void proposeSpecifications(EObject model, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		try {
			final IFile me =  CommonUtil.eObject2IFile(model);
			final IContainer folder = (IContainer) me.getParent();
			acceptor.accept(super.createCompletionProposal("DPF", context));
			for (final IResource r : folder.members()) {
				if (r instanceof IFile && r != me) {
					final int i = r.getName().lastIndexOf(DPFConstants.SPECIFICATION_FILE_EXTENSION);
					if (-1 < i && !r.getName().startsWith(".")) {
						acceptor.accept(super.createCompletionProposal(r.getName().substring(0, i), context));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
