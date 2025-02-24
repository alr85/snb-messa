package com.example.mecca

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person // Importing Person icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun ServiceSelectCustomerScreen(navController: NavHostController, db: AppDatabase, repository: CustomerRepository) {
    var searchQuery by remember { mutableStateOf("") }
    val customerList = remember { mutableStateOf<List<CustomerLocal>>(emptyList()) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Fetch the list of customers from the database only once
    LaunchedEffect(Unit) {
        customerList.value = repository.getCustomersFromDb()
    }

    // Use a Box to overlay the content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Set the entire screen background to white
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Select a Customer",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                style = TextStyle(
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            )

            // Search bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(
                        width = 2.dp,
                        color = Color.Gray,
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                placeholder = { Text("Search...") },
            )

            // Filtered and sorted customer list
            val filteredCustomers = customerList.value.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }.sortedBy { it.name }

            LazyColumn(modifier = Modifier.fillMaxHeight(1.0f)) {
                items(filteredCustomers) { customer ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 0.dp) // Removed horizontal padding
                                .clickable {
                                    // Navigate to start calibration immediately on selection
                                    navController.navigate("calibrationSearchSystem/${customer.fusionID}/${customer.name}/${customer.postcode}")
                                    keyboardController?.hide() // Hide keyboard if it's visible
                                },
                            verticalAlignment = Alignment.CenterVertically // Align items vertically
                        ) {
                            // Customer Icon
                            Icon(
                                imageVector = Icons.Filled.Person, // Replace with desired icon
                                contentDescription = "Customer Icon",
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(end = 8.dp) // Space between icon and text
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) { // Adjust padding inside the Box
                                    Text(
                                        text = customer.name,
                                        modifier = Modifier.padding(bottom = 4.dp),
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "${customer.customerCityTown}, ${customer.postcode}"
                                    )
                                }
                            }
                        }
                        // Inset Divider moved outside the Box
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 1.dp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}
