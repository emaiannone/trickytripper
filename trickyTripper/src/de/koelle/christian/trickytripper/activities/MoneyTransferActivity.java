package de.koelle.christian.trickytripper.activities;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import de.koelle.christian.common.utils.NumberUtils;
import de.koelle.christian.common.utils.UiUtils;
import de.koelle.christian.trickytripper.R;
import de.koelle.christian.trickytripper.TrickyTripperApp;
import de.koelle.christian.trickytripper.activitysupport.PopupFactory;
import de.koelle.christian.trickytripper.constants.Rc;
import de.koelle.christian.trickytripper.constants.Rx;
import de.koelle.christian.trickytripper.controller.TripExpensesFktnController;
import de.koelle.christian.trickytripper.factories.AmountFactory;
import de.koelle.christian.trickytripper.factories.ModelFactory;
import de.koelle.christian.trickytripper.model.Amount;
import de.koelle.christian.trickytripper.model.Debts;
import de.koelle.christian.trickytripper.model.Participant;
import de.koelle.christian.trickytripper.model.Payment;
import de.koelle.christian.trickytripper.model.PaymentCategory;
import de.koelle.christian.trickytripper.modelutils.AmountViewUtils;

public class MoneyTransferActivity extends Activity {

    private final Map<Participant, Amount> amountByParticipant = new HashMap<Participant, Amount>();
    private Participant transferer;
    private Amount amountTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.money_transfer_view);

        transferer = (Participant) getIntent().getExtras().getSerializable(
                Rc.ACTIVITY_PARAM_KEY_PARTICIPANT);
        List<Participant> allParticipants = getFktnController().getAllParticipants(false);
        Debts debtsOfTransferer = getFktnController().getDebts().get(transferer);

        initView(transferer, allParticipants, debtsOfTransferer);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.general_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.general_options_help:
            showDialog(Rc.DIALOG_SHOW_HELP);
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        Dialog dialog;
        switch (id) {
        case Rc.DIALOG_SHOW_HELP:
            dialog = PopupFactory.createHelpDialog(this, getApp().getFktnController(), Rc.DIALOG_SHOW_HELP);
            break;
        default:
            dialog = null;
        }

        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        switch (id) {
        case Rc.DIALOG_SHOW_HELP:
            // intentionally blank
            break;
        default:
            dialog = null;
        }
        super.onPrepareDialog(id, dialog, args);
    }

    private void initView(Participant transferer, List<Participant> allParticipants, Debts debtsOfTransferer) {
        createDynamicTable(transferer, allParticipants, debtsOfTransferer);
    }

    private void createDynamicTable(Participant transferer, List<Participant> allParticipants, Debts debtsOfTransferer) {
        TableLayout listView = (TableLayout) findViewById(R.id.money_transfer_view_table_layout);
        addDynamicRows(transferer, allParticipants, debtsOfTransferer, listView);
    }

    /**
     * View method.
     * 
     * @param view
     *            Required parameter.
     */
    public void notPartOfThisRelease(View view) {
        Toast.makeText(
                getApplicationContext(),
                R.string.common_toast_currency_not_part_of_this_release,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * View method.
     * 
     * @param view
     *            Required parameter.
     */
    public void cancel(View view) {
        finish();
    }

    /**
     * View method.
     * 
     * @param view
     *            Required parameter.
     */
    public void transfer(View view) {
        createNewPayments();
        finish();
    }

    private void createNewPayments() {
        for (Entry<Participant, Amount> entry : amountByParticipant.entrySet()) {
            Amount amountInput = entry.getValue();
            if (amountInput.getValue() > 0) {
                Amount amountInputNeg = amountInput.clone();
                amountInputNeg.setValue(NumberUtils.neg(amountInputNeg.getValue()));
                Payment newPayment = ModelFactory.createNewPayment(
                        getResources().getString(PaymentCategory.MONEY_TRANSFER.getResourceStringId()),
                        PaymentCategory.MONEY_TRANSFER);
                newPayment.getParticipantToPayment().put(entry.getKey(), amountInputNeg);
                newPayment.getParticipantToSpending().put(transferer, amountInput);
                getFktnController().persistPayment(newPayment);
            }
        }
    }

    private void addDynamicRows(Participant transferer, List<Participant> allParticipants,
            Debts debtsOfTransferer, TableLayout tableLayout) {

        TextView textView = ((TextView) findViewById(R.id.money_transfer_view_output_participant_from));
        textView.setText(transferer.getName());

        List<Participant> allToBe;
        allToBe = getAllToBeListed(transferer, allParticipants, debtsOfTransferer, true);
        addTransferRow(debtsOfTransferer, tableLayout, allToBe, true);
        allToBe = getAllToBeListed(transferer, allParticipants, debtsOfTransferer, false);
        addTransferRow(debtsOfTransferer, tableLayout, allToBe, false);

    }

    private void addTransferRow(Debts debtsOfTransferer, TableLayout tableLayout, List<Participant> allToBe,
            boolean firstAddRows) {
        TableRow newRow;
        Button buttonDueAmount;
        Button buttonCurrency;
        TextView nameTextView;
        int offset = tableLayout.getChildCount();

        int dynViewId = (firstAddRows) ? Rx.DYN_ID_PAYMENT_MONEY_TRANSFER_FIRST
                : Rx.DYN_ID_PAYMENT_MONEY_TRANSFER_SECOND;

        for (int i = 0; i < allToBe.size(); i++) {
            Participant p = allToBe.get(i);
            final Amount amountDue = getNullSafeDebts(debtsOfTransferer, p);
            final Amount inputValueModel = getAmountFac().createAmount();
            newRow = (TableRow) inflate(R.layout.money_transfer_list_view);

            final EditText editText = (EditText) newRow.findViewById(R.id.money_transfer_list_view_input_amount);
            nameTextView = (TextView) newRow.findViewById(R.id.money_transfer_list_view_output_name);
            buttonDueAmount = (Button) newRow.findViewById(R.id.money_transfer_list_view_button_due_amount);
            buttonCurrency = (Button) newRow.findViewById(R.id.money_transfer_list_view_button_currency);

            UiUtils.makeProperNumberInput(editText, getLocale());
            editText.setId(dynViewId);
            tableLayout.addView(newRow, i + offset);
            nameTextView.setText(p.getName());
            UiUtils.setFontAndStyle(this, nameTextView, !p.isActive(), android.R.style.TextAppearance_Small);

            buttonCurrency.setText(getFktnController().getTripLoaded().getBaseCurrency().getSymbol());

            if (amountDue == null) {
                buttonDueAmount.setEnabled(false);
                buttonDueAmount.setText("0");
            }
            else {
                buttonDueAmount.setText(AmountViewUtils.getAmountString(getLocale(), amountDue, true, true));
                buttonDueAmount.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        editText.setText(AmountViewUtils.getAmountString(getLocale(), amountDue, true, true));
                    }
                });
            }
            bindPaymentInputAndUpdate(editText, inputValueModel);
            amountByParticipant.put(p, inputValueModel);
            dynViewId++;
        }
        updateSum();
        updateSaveButtonState();
    }

    private void bindPaymentInputAndUpdate(final EditText widget, final Amount amount) {

        widget.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                amount.setValue(NumberUtils.getStringToDouble(getLocale(), s.toString()));
                MoneyTransferActivity.this.updateSum();
                MoneyTransferActivity.this.updateSaveButtonState();
            }
        });
    }

    protected void updateSum() {
        Locale locale = getLocale();
        amountTotal = calculateTotalSum();
        TextView textView = (TextView) findViewById(R.id.money_transfer_view_output_total_transfer_amount);
        textView.setText(AmountViewUtils.getAmountString(locale, amountTotal, false, false, false, true, true));
    }

    private void updateSaveButtonState() {
        Button saveButton = (Button) findViewById(R.id.money_transfer_view_button_transfer);
        saveButton.setEnabled(amountTotal.getValue() > 0);
    }

    private Amount calculateTotalSum() {
        Amount amountTotal = getAmountFac().createAmount();
        for (Entry<Participant, Amount> pair : amountByParticipant.entrySet()) {
            amountTotal.addAmount(pair.getValue());
        }
        return amountTotal;
    }

    private Amount getNullSafeDebts(Debts debtsOfTransferer, Participant p) {
        final Amount a = (
                debtsOfTransferer != null
                        && debtsOfTransferer.getLoanerToDepts() != null
                        && debtsOfTransferer.getLoanerToDepts().get(p) != null
                        && debtsOfTransferer.getLoanerToDepts().get(p).getValue() > 0) ?

                        debtsOfTransferer.getLoanerToDepts().get(p) :
                        null;
        return a;
    }

    private List<Participant> getAllToBeListed(Participant transferer, List<Participant> allParticipants,
            Debts debtsOfTransferer, boolean onlyOwing) {
        List<Participant> result = new ArrayList<Participant>();
        for (Participant p : allParticipants) {
            if (!p.equals(transferer)) {
                Amount owingAmount = getNullSafeDebts(debtsOfTransferer, p);
                if (onlyOwing && owingAmount != null) {
                    result.add(p);
                }
                else if (!onlyOwing && owingAmount == null) {
                    result.add(p);
                }
            }
        }
        final Collator collator = getFktnController().getDefaultStringCollator();
        Collections.sort(result, new Comparator<Participant>() {
            public int compare(Participant object1, Participant object2) {
                return collator.compare(object1.getName(), object2.getName());
            }
        });
        return result;
    }

    private View inflate(int layoutId) {
        LayoutInflater inflater = getLayoutInflater();
        final View viewInf = inflater.inflate(layoutId, null);
        return viewInf;
    }

    private TrickyTripperApp getApp() {
        TrickyTripperApp app = ((TrickyTripperApp) getApplication());
        return app;
    }

    private Locale getLocale() {
        Locale locale = getResources().getConfiguration().locale;
        return locale;
    }

    private AmountFactory getAmountFac() {

        return getApp().getAmountFactory();
    }

    private TripExpensesFktnController getFktnController() {
        return getApp().getFktnController();
    }

}