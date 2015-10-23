package doser.entity.disambiguation.feedback;

import doser.entitydisambiguation.feedback.dpo.RequestFeedbackProxy;

/**
 * ToDo HBase Server Callup
 * 
 * 
 * @author quh
 *
 */
public class Feedback {

	private static volatile Feedback instance = null;

	private Feedback() {
	}

	public static Feedback getInstance() {
		if (instance == null) {
			synchronized (Feedback.class) {
				if (instance == null) {
					instance = new Feedback();
				}
			}
		}
		return instance;
	}
	
	public String setFinalFeedback(final RequestFeedbackProxy request) {
		String response = "";
//		try {
//			final List<FeedbackItem> lst = request.getFeedbackItems();
//			for (final FeedbackItem feedbackItem2 : lst) {
//				final FeedbackItem feedbackItem = feedbackItem2;
//				final int rowKey = createQueryHash(request.getDocId(),
//						feedbackItem.getSelectedText(),
//						feedbackItem.getPosition());
//				final int rowVersion = HBaseOperations.getAmountRowVersions(
//						HBaseOperations.TABLENAMES[0], String.valueOf(rowKey),
//						11);
//				if (rowVersion > 0) {
//					String value = HBaseOperations.removeHbaseEntry(
//							HBaseOperations.TABLENAMES[0], String
//									.valueOf(rowKey), request
//									.getCurrentFamily(), String
//									.valueOf(feedbackItem.getEntityUri()
//											.hashCode()), String
//									.valueOf(rowVersion), 11);
//					if (value != null) {
//						final StringBuffer buffer = new StringBuffer();
//						value = buffer.append(feedbackItem.getTypeOfFeedback())
//								.append(" ").append(value).toString();
//
//						final int rowVersionNT = HBaseOperations
//								.getAmountRowVersions(request.getTableName(),
//										String.valueOf(rowKey), 11);
//
//						final String rowkey = HBaseOperations.transformRowKey(
//								String.valueOf(rowKey), 11)
//								+ "_"
//								+ HBaseOperations.transformRowKey(
//										String.valueOf(rowVersionNT + 1), 2);
//
//						HBaseOperations.addRecord(
//								request.getTableName(),
//								rowkey,
//								request.getCurrentFamily(),
//								String.valueOf(feedbackItem.getEntityUri()
//										.hashCode()),
//								feedbackItem.getEntityUri() + "-"
//										+ request.getContext());
//					}
//				}
//			}
//		} catch (final IOException e) {
//			response = "Fail_Adding_Feedback";
//		}
		return response;
	}

	public String setQueryResult(final RequestFeedbackProxy request) {
		String response = "";
//		try {
//			final int rowVersion = HBaseOperations.getAmountRowVersions(
//					request.getTableName(), request.getRowKey(), 11);
//			final String rowkey = HBaseOperations.transformRowKey(
//					request.getRowKey(), 11)
//					+ "_"
//					+ HBaseOperations.transformRowKey(
//							String.valueOf(rowVersion + 1), 2);
//			String[] uniqueEntityUris = request.getUniqueEntityUri();
//			for (int i = 0; i < uniqueEntityUris.length; i++) {
//				HBaseOperations.addRecord(request.getTableName(), rowkey,
//						request.getCurrentFamily(),
//						String.valueOf(uniqueEntityUris[i].hashCode()),
//						uniqueEntityUris[i] + "-" + request.getSurfaceForms()
//								+ "-" + request.getContext());
//			}
//			response = "Ok_Add_Record";
//		} catch (final IOException e) {
//			response = "Fail_Adding_Records";
//		}
		return response;
	}

//	private static int createQueryHash(final String docId, final String text,
//			final List<Position> posList) {
//
//		final StringBuffer buffer = new StringBuffer();
//		if (posList != null) {
//			for (final Position position2 : posList) {
//				final Position position = position2;
//				buffer.append(position.toString());
//			}
//		}
//
//		final String uniqueQuery = new StringBuffer().append(docId)
//				.append(text.toLowerCase(Locale.US)).append(buffer.toString())
//				.toString();
//		return uniqueQuery.hashCode();
//	}

}
