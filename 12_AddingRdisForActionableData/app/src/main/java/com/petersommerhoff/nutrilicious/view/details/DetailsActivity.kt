package com.petersommerhoff.nutrilicious.view.details

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.petersommerhoff.nutrilicious.R
import com.petersommerhoff.nutrilicious.model.*
import com.petersommerhoff.nutrilicious.view.common.getViewModel
import com.petersommerhoff.nutrilicious.viewmodel.DetailsViewModel
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.*

const val FOOD_ID_EXTRA = "NDBNO"

class DetailsActivity : AppCompatActivity() {

  private lateinit var detailsViewModel: DetailsViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_details)

    detailsViewModel = getViewModel(DetailsViewModel::class)
    val foodId = intent.getStringExtra(FOOD_ID_EXTRA)
    updateUiWith(foodId)
  }

  private fun updateUiWith(foodId: String) {
    if (foodId.isBlank()) return

    launch {
      val details = detailsViewModel.getDetails(foodId)
      withContext(UI) { bindUi(details) }
    }
  }

  private fun bindUi(details: FoodDetails?) {
    if (details != null) {
      tvFoodName.text = "${details.name} (100g)"
      tvProximates.text = makeSection(details, NutrientType.PROXIMATES)
      tvMinerals.text = makeSection(details, NutrientType.MINERALS)
      tvVitamins.text = makeSection(details, NutrientType.VITAMINS)
      tvLipids.text = makeSection(details, NutrientType.LIPIDS)
    } else {
      tvFoodName.text = getString(R.string.no_data)
    }
  }

  private fun makeSection(details: FoodDetails, forType: NutrientType) =
      details.nutrients.filter { it.type == forType }
          .joinToString(separator = "\n", transform = ::renderNutrient)

  private fun renderNutrient(nutrient: Nutrient): String = with(nutrient) {
    val name = name.substringBefore(",")  // = whole name if no comma
    val amount = amountPer100g.value.render()
    val unit = amountPer100g.unit
    val percent = getPercentOfRdi(nutrient).render()
    val rdiNote = if (percent.isNotEmpty()) "($percent% of RDI)" else ""
    "$name: $amount$unit $rdiNote"
  }

  private fun Double.render() = if (this >= 0.0) "%.2f".format(this) else ""

  private fun getPercentOfRdi(nutrient: Nutrient): Double {
    val nutrientAmount: Double = nutrient.amountPer100g.normalized()
    val rdi: Double = RDI[nutrient.id]?.normalized() ?: return -1.0

    return nutrientAmount / rdi * 100
  }
}
