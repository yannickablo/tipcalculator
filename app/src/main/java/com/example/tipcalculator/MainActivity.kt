package com.example.tipcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tipcalculator.ui.theme.TipCalculatorTheme
import java.text.NumberFormat

// Point d'entrée principal de l'application
class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        // Active le rendu bord à bord pour un affichage plein écran moderne
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Définit le contenu de l'interface utilisateur en utilisant Jetpack Compose
        setContent {
            // Applique le thème de l'application
            TipCalculatorTheme {
                // Surface principale pour contenir le contenu, remplissant l'écran
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Compose principale pour l'agencement du calculateur de pourboire
                    TipTimeLayout()
                }
            }
        }
    }
}

/**
 * Composant principal qui gère l'agencement de l'écran du calculateur de pourboire.
 * Il contient les champs de saisie, le bouton bascule pour l'arrondi et le résultat du calcul.
 */
@Composable
fun TipTimeLayout() {
    // Déclaration de l'état (variables mutables) pour les champs de saisie et l'option d'arrondi.
    // L'utilisation de 'remember' et 'mutableStateOf' permet à Compose de suivre et de mettre
    // à jour l'interface utilisateur lorsque ces valeurs changent.

    // État pour le montant de la facture (String)
    var amountInput by remember { mutableStateOf("") }
    // État pour le pourcentage de pourboire (String)
    var tipInput by remember { mutableStateOf("") }
    // État pour l'option d'arrondi du pourboire (Boolean)
    var roundUp by remember { mutableStateOf(false) }

    // Conversion des chaînes de saisie en Double.
    // 'toDoubleOrNull()' tente la conversion, et utilise 0.0 si la chaîne est vide ou invalide.
    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val tipPercent = tipInput.toDoubleOrNull() ?: 0.0

    // Calcul du pourboire en appelant la fonction 'calculateTip'
    val tip = calculateTip(amount, tipPercent, roundUp)

    // Conteneur principal en colonne (vertical) avec des modificateurs pour le style et le défilement.
    Column(
        modifier = Modifier
            .statusBarsPadding() // Gère la marge pour la barre de statut
            .padding(horizontal = 40.dp) // Marge horizontale
            .verticalScroll(rememberScrollState()) // Permet le défilement vertical
            .safeDrawingPadding(), // Gère les marges sécurisées (encoches, barres de navigation)
        horizontalAlignment = Alignment.CenterHorizontally, // Centre les éléments horizontalement
        verticalArrangement = Arrangement.Center // Centre les éléments verticalement
    ) {
        // Titre de l'application
        Text(
            text = stringResource(R.string.calculate_tip),
            modifier = Modifier
                .padding(bottom = 16.dp, top = 40.dp)
                .align(alignment = Alignment.Start) // Alignement à gauche
        )

        // Champ de saisie pour le Montant de la facture
        EditNumberField(
            label = R.string.bill_amount, // Libellé (ressource String)
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number, // Clavier numérique
                imeAction = ImeAction.Next // Action 'Suivant' sur le clavier
            ),
            value = amountInput, // Valeur actuelle
            // Callback lorsque la valeur change
            onValueChange = { amountInput = it },
            // Icône décorative
            leadingIcon = R.drawable.money,
            // Modificateur de style
            modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth()
        )

        // Champ de saisie pour le Pourcentage de pourboire
        EditNumberField(
            label = R.string.how_was_the_service, // Libellé
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number, // Clavier numérique
                imeAction = ImeAction.Done // Action 'Terminé' sur le clavier
            ),
            value = tipInput,
            onValueChange = { tipInput = it },
            // Icône décorative
            leadingIcon = R.drawable.percent,
            modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth()
        )

        // Ligne pour le bouton bascule d'arrondi
        RoundTheTipRow(
            roundUp = roundUp, // État actuel de l'arrondi
            onRoundUpChanged = { roundUp = it }, // Callback lorsque l'état change
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Affichage du montant du pourboire calculé
        Text(
            text = stringResource(R.string.tip_amount, tip), // Affiche le texte formaté avec le montant du pourboire
            style = MaterialTheme.typography.displaySmall // Style de texte
        )
        // Espace vide en bas pour le défilement
        Spacer(modifier = Modifier.height(150.dp))
    }
}

/**
 * Composant réutilisable pour le champ de saisie de texte (TextField).
 * Il est conçu pour les entrées numériques avec des options spécifiques.
 */
@Composable
fun EditNumberField(
    @StringRes label: Int, // Réf. de la ressource String pour le libellé
    @DrawableRes leadingIcon: Int, // Réf. de la ressource Drawable pour l'icône
    keyboardOptions: KeyboardOptions, // Options du clavier virtuel
    value: String, // Valeur actuelle du champ
    onValueChange: (String) -> Unit, // Fonction de rappel pour la mise à jour de la valeur
    modifier: Modifier = Modifier
) {
    // Utilisation du composant TextField de Material 3
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) }, // Affiche le libellé
        singleLine = true, // Force la saisie sur une seule ligne
        keyboardOptions = keyboardOptions, // Applique les options du clavier
        leadingIcon = { Icon(painter = painterResource(id = leadingIcon), contentDescription = null) }, // Affiche l'icône décorative
        modifier = modifier,
    )
}

/**
 * Composant pour la ligne contenant le libellé "Arrondir le pourboire ?" et le bouton bascule (Switch).
 */
@Composable
fun RoundTheTipRow(
    roundUp: Boolean, // État actuel de l'arrondi
    onRoundUpChanged: (Boolean) -> Unit, // Fonction de rappel pour changer l'état
    modifier: Modifier = Modifier
) {
    // Conteneur en ligne (horizontal)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .size(48.dp), // Taille prédéfinie pour la ligne
        verticalAlignment = Alignment.CenterVertically // Aligne verticalement au centre
    ) {
        // Texte du libellé "Arrondir le pourboire ?"
        Text(text = stringResource(R.string.round_up_tip))

        // Composant Switch (bouton bascule)
        Switch(
            checked = roundUp, // État du bouton bascule
            onCheckedChange = onRoundUpChanged, // Mise à jour de l'état
            modifier = modifier
                .fillMaxWidth() // Remplit la largeur restante
                .wrapContentWidth(Alignment.End) // Aligne le Switch à la fin de la ligne
        )
    }
}


/**
 * Calcule le pourboire en fonction du montant, du pourcentage et de l'option d'arrondi,
 * puis formate le résultat dans la devise locale.
 */
fun calculateTip(
    amount: Double, // Montant de la facture
    tipPercent: Double = 15.0, // Pourcentage de pourboire par défaut (mais écrasé par la saisie)
    roundUp: Boolean // Option d'arrondi
): String {
    // Calcul du pourboire : (pourcentage / 100) * montant
    var tip = tipPercent / 100 * amount

    // Si l'option d'arrondi est activée
    if (roundUp) {
        // Arrondit le pourboire à l'entier supérieur (plafond)
        tip = kotlin.math.ceil(tip)
    }

    // Formate le montant du pourboire dans la devise locale (ex: "$10.00")
    return NumberFormat.getCurrencyInstance().format(tip)
}

/**
 * Fonction de prévisualisation pour le composant TipTimeLayout dans Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun TipTimeLayoutPreview() {
    TipCalculatorTheme {
        TipTimeLayout()
    }
}