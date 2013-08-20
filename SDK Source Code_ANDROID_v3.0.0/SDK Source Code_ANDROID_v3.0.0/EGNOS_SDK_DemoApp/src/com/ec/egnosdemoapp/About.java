/**
 * @file About.java
 *
 * Displays the Disclaimer for the "EGNOS-SDK".
 * 
 * Rev: 3.0.0
 * 
 * Author: DKE Aerospace Germany GmbH
 *
 * Copyright 2012 European Commission
 *
 * Licensed under the EUPL, Version 1.1 only (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 *
 **/
package com.ec.egnosdemoapp;

import com.ec.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Class that displays the Disclaimer for the "EGNOS-SDK".
 **/
public class About extends Activity {

  /**
   * onCreate function 
   * Called on start of activity, displays the Disclaimer for the "EGNOS-SDK".
   */
  @Override
  public final void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.about);

    TextView aboutTextView = (TextView) this.findViewById(R.id.aboutTextView);
    String aboutText = "       Disclaimer for the \"EGNOS SDK v.3.0.0\" \n\n"
        + "  \"EGNOS SDK\" is offered for developing applications "
        + "   for the EGNOS Open Service only. The terms and "
        + "   conditions of using this service, including the"
        + "   applicable limitations of liability, are defined"
        + "   in the EGNOS OS Service Definitions Document. "
        + "   Please consult it at http://ec.europa.eu/enterprise/"
        + "   policies/satnav/egnos/open-service/index_en.htm before using"
        + "   the \"EGNOS SDK\". \n\n"
        + "   ESA SISNeT service disclaimer (http://www.egnos-pro.esa.int/"
        + "   sisnet/disclaimer.html) will apply for the users of this service.\n\n"
        + "   \"EGNOS SDK\" is offered on an \"as is\" basis. The use of the "
        + "   \"EGNOS SDK\" is therefore at the user’s own risk. The European Commission "
        + "   expressly disclaims all warranties of any kind (whether express or implied),"
        + "   including, but not limited to the implied warranties of fitness for a particular"
        + "   purpose. No advice or information, whether oral or written, obtained by a user"
        + "   from the European Commission shall create any warranty. \n\n"
        + "   By using the \"EGNOS SDK\", the user agrees that the European Union"
        + "   represented by the Commission shall not be held responsible or liable"
        + "   for any direct, indirect, incidental, special or consequential damages, "
        + "   including but not limited to, damages for interruption of business, loss of profits,"
        + "   goodwill or other intangible losses, resulting from the use and/or resulting from"
        + "   the misuse and/or impossibility to retrieve the EGNOS corrections or resulting"
        + "   from any other circumstance experienced by the user of the EGNOS SDK.";

    aboutTextView.setText(aboutText);
    
     TextView poweredByTextview = (TextView)this.findViewById(R.id.poweredByTextView);
     poweredByTextview.setText("Powered By ");
     
     TextView logo1Textview = (TextView)this.findViewById(R.id.logo1textView);
     logo1Textview.setText("DKE Aerospace Germany GmbH");
     
     TextView logo2Textview = (TextView)this.findViewById(R.id.logo2textView);
     logo2Textview.setText("Valdani Vicari & Associati Srl");
  }
}
