package org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldLoadActivityMapDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldLoadResourceTableDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldPopulateActivitiesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldPopulateResourceTableChain;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.AbstractPlannerField;

public interface IPlannerFieldExtension<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI, OWNER extends AbstractPlannerField<T, P, RI, AI>> extends IFormFieldExtension<OWNER> {

  void execPopulateActivities(PlannerFieldPopulateActivitiesChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain, List<RI> resourceIds, List<ITableRow> resourceRows) throws ProcessingException;

  Object[][] execLoadResourceTableData(PlannerFieldLoadResourceTableDataChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain) throws ProcessingException;

  void execPopulateResourceTable(PlannerFieldPopulateResourceTableChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain) throws ProcessingException;

  Object[][] execLoadActivityMapData(PlannerFieldLoadActivityMapDataChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain, List<? extends RI> resourceIds, List<? extends ITableRow> resourceRows) throws ProcessingException;
}
